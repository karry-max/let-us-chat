package com.yxp.chat.common.websocket.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yxp.chat.common.common.config.ThreadPoolConfig;
import com.yxp.chat.common.common.constant.RedisKey;
import com.yxp.chat.common.common.event.UserOfflineEvent;
import com.yxp.chat.common.common.event.UserOnlineEvent;
import com.yxp.chat.common.common.utils.RedisUtils;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.domain.enums.RoleEnum;
import com.yxp.chat.common.user.service.IRoleService;
import com.yxp.chat.common.user.service.LoginService;
import com.yxp.chat.common.user.service.cache.UserCache;
import com.yxp.chat.common.websocket.NettyUtil;
import com.yxp.chat.common.websocket.domain.dto.WSChannelExtraDTO;
import com.yxp.chat.common.websocket.service.WebSocketService;
import com.yxp.chat.common.websocket.service.adapter.WebSocketAdapter;
import com.yxp.chat.common.user.service.adapter.WSAdapter;
import com.yxp.chat.common.websocket.domain.vo.resp.WSBaseResp;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.redisson.client.ChannelName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 专门管理websocket的逻辑，包括推拉
 */
@Service
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    /**
     * 管理所有用户的连接（登录态/游客） 在线列表
     */
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    public static final int MAXIMUM_SIZE = 1000;
    public static final Duration DURATION = Duration.ofHours(1);


    /**
     * redis保存loginCode的key
     */
    private static final String LOGIN_CODE = "loginCode";
    private static final Duration EXPIRE_TIME = Duration.ofHours(1);

    /**
     * 临时保存登录code和channel的映射关系
     */
    private static final Cache<Integer, Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterWrite(DURATION)
            .build();

    /**
     * 所有在线的用户和对应的socket
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();

    @Lazy
    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private UserCache userCache;

    @Autowired
    @Qualifier(ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 当web客户端连接时触发 此时uid为null
     * @param channel
     */
    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, new WSChannelExtraDTO());
    }

    /**
     * 处理登录请求
     * @param channel
     */
    @Override
    public void handleLoginReq(Channel channel) throws WxErrorException {
        //1.生成随机二维码
        Integer code = generateLoginCode(channel);
        //2.找微信申请带参二维码
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, (int) DURATION.getSeconds());
        //3.把码推送给前端
        sendMsg(WebSocketAdapter.buildResp(wxMpQrCodeTicket), channel);
    }




    /**
     * 发送信息给所有人
     * @param msg
     */
    @Override
    public void sendMsgToAll(WSBaseResp<?> msg) {
        ONLINE_WS_MAP.forEach((channel, ext) -> {
            threadPoolTaskExecutor.execute(() -> sendMsg(msg, channel));
        });
    }

    /**
     * 获取不重复的登录的code，微信要求最大不超过int的存储极限
     * 防止并发，可以给方法加上synchronize，也可以使用cas乐观锁
     *
     * @return
     */
    private Integer generateLoginCode(Channel channel) {
        int inc;
        do {
            //本地cache时间必须比redis key过期时间短，否则会出现并发问题
            inc = RedisUtils.integerInc(RedisKey.getKey(LOGIN_CODE), (int) EXPIRE_TIME.toMinutes(), TimeUnit.MINUTES);
        } while (WAIT_LOGIN_MAP.asMap().containsKey(inc));
        //储存一份在本地
        WAIT_LOGIN_MAP.put(inc, channel);
        return inc;
    }

    /**
     * 处理ws断开连接的事件
     *
     * @param channel
     */
    @Override
    public void removed(Channel channel) {
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        Optional<Long> uidOptional = Optional.ofNullable(wsChannelExtraDTO)
                .map(WSChannelExtraDTO::getUid);
        boolean offlineAll = offline(channel, uidOptional);
        if (uidOptional.isPresent() && offlineAll) {//已登录用户断连,并且全下线成功
            User user = new User();
            user.setId(uidOptional.get());
            user.setLastOptTime(new Date());
            applicationEventPublisher.publishEvent(new UserOfflineEvent(this, user));
        }
    }

    /**
     * 用户认证
     * @param channel
     * @param token
     */
    @Override
    public void authorize(Channel channel, String token) {
        //验证token是否还有效
        Long validUid = loginService.getValidUid(token);
        if(Objects.nonNull(validUid)){
            User user = userDao.getById(validUid);
            //sendMsg(WebSocketAdapter.buildResp(user, token), channel);
            loginSuccess(channel, user, token);
        }else{
            sendMsg(WebSocketAdapter.buildInvalidTokenResp(), channel);
        }
    }

    /**
     * 登录成功后的业务
     * @param channel
     * @param user   用户扫码登录成功scanLoginSuccess创建完token后调用它
     * @param token
     */
    private void loginSuccess(Channel channel, User user, String token) {
        //更新上线列表
        online(channel, user.getId());

        //推送成功消息 这里还推送用户是否拥有管理员的权限的权限
        sendMsg(WebSocketAdapter.buildResp(user, token, roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER)), channel);

        //发送用户上线事件
        boolean online = userCache.isOnline(user.getId());
        //用户上线成功的事件
        if (!online) {
            user.setLastOptTime(new Date());
            user.refreshIp(NettyUtil.getAttr(channel, NettyUtil.IP));
            applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
        }
    }

    /**
     * 用户上线
     */
    private void online(Channel channel, Long uid) {
        //相当于构建（channel， uid）只不过对uid进行了包装
        getOrInitChannelExt(channel).setUid(uid);
        //这里又是（uid）和channel
        ONLINE_UID_MAP.putIfAbsent(uid, new CopyOnWriteArrayList<>());
        ONLINE_UID_MAP.get(uid).add(channel);
        NettyUtil.setAttr(channel, NettyUtil.UID, uid);
    }

    /**
     * 用户下线的处理
     * @param channel
     */
    @Override
    public boolean offline(Channel channel, Optional<Long> uidOptional) {
        ONLINE_WS_MAP.remove(channel);
        //用户下线
        if (uidOptional.isPresent()) {
            CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uidOptional.get());
            if (CollectionUtil.isNotEmpty(channels)) {
                channels.removeIf(ch -> Objects.equals(ch, channel));
            }
            return CollectionUtil.isEmpty(ONLINE_UID_MAP.get(uidOptional.get()));
        }
        return true;
    }


    /**
     * 用户扫码登录成功   用户已经注册和授权后走它，或者第一次注册和授权完后还是走它
     * @param code
     * @param uid
     */
    @Override
    public void scanLoginSuccess(Integer code, Long uid) {
        //1.确认链接在机器上
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if(Objects.isNull(channel)){
            return;
        }
        User user = userDao.getById(uid);
        //2.移除code
        WAIT_LOGIN_MAP.invalidate(code);
        //3.调用登录模块获取token
        String token = loginService.login(uid);

        //4.用户登录
        //sendMsg(WebSocketAdapter.buildResp(user, token), channel);
        loginSuccess(channel, user, token);
    }


    @Override
    public Boolean scanSuccess(Integer loginCode) {
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(loginCode);
        if (Objects.nonNull(channel)) {
            sendMsg(WSAdapter.buildScanSuccessResp(), channel);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 如果在线列表不存在，就先把该channel放进在线列表
     *
     * @param channel
     * @return
     */
    private WSChannelExtraDTO getOrInitChannelExt(Channel channel) {
        WSChannelExtraDTO wsChannelExtraDTO =
                ONLINE_WS_MAP.getOrDefault(channel, new WSChannelExtraDTO());
        WSChannelExtraDTO old = ONLINE_WS_MAP.putIfAbsent(channel, wsChannelExtraDTO);
        return ObjectUtil.isNull(old) ? wsChannelExtraDTO : old;
    }

    //entrySet的值不是快照数据,但是它支持遍历，所以无所谓了，不用快照也行。
    @Override
    public void sendToAllOnline(WSBaseResp<?> wsBaseResp, Long skipUid) {
        ONLINE_WS_MAP.forEach((channel, ext) -> {
            if (Objects.nonNull(skipUid) && Objects.equals(ext.getUid(), skipUid)) {
                return;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(wsBaseResp, channel));
        });
    }

    @Override
    public void sendToAllOnline(WSBaseResp<?> wsBaseResp) {
        sendToAllOnline(wsBaseResp, null);
    }

    @Override
    public void sendToUid(WSBaseResp<?> wsBaseResp, Long uid) {
        CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uid);
        if (CollectionUtil.isEmpty(channels)) {
            log.info("用户：{}不在线", uid);
            return;
        }
        channels.forEach(channel -> {
            threadPoolTaskExecutor.execute(() -> sendMsg(wsBaseResp, channel));
        });
    }

    /**
     * 发送消息给前端 设置为泛型通用
     * @param resp
     * @param channel
     */
    private void sendMsg(WSBaseResp<?> resp, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(resp)));
    }


}
