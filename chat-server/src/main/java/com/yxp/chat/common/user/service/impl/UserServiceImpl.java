package com.yxp.chat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yxp.chat.common.common.annotation.RedissonLock;
import com.yxp.chat.common.common.event.UserBlackEvent;
import com.yxp.chat.common.common.event.UserRegisterEvent;
import com.yxp.chat.common.common.utils.AssertUtil;
import com.yxp.chat.common.user.dao.BlackDao;
import com.yxp.chat.common.user.domain.entity.*;
import com.yxp.chat.common.user.domain.vo.req.*;
import com.yxp.chat.common.user.service.UserService;
import com.yxp.chat.common.user.service.adapter.UserAdapter;
import com.yxp.chat.common.user.service.cache.ItemCache;
import com.yxp.chat.common.user.service.cache.UserCache;
import com.yxp.chat.common.user.service.cache.UserSummaryCache;
import com.yxp.chat.common.user.dao.ItemConfigDao;
import com.yxp.chat.common.user.dao.UserBackpackDao;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.user.domain.dto.ItemInfoDTO;
import com.yxp.chat.common.user.domain.dto.SummeryInfoDTO;
import com.yxp.chat.common.user.domain.enums.BlackTypeEnum;
import com.yxp.chat.common.user.domain.enums.ItemEnum;
import com.yxp.chat.common.user.domain.enums.ItemTypeEnum;
import com.yxp.chat.common.user.domain.vo.resp.BadgeResp;
import com.yxp.chat.common.user.domain.vo.resp.UserInfoResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private ItemCache itemCache;

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private UserCache userCache;

    @Autowired
    private BlackDao blackDao;
    
    @Autowired
    private UserSummaryCache userSummaryCache;

    /**
     * 用户注册
     * @param insert
     * @return
     */
    @Override
    @Transactional
    public Long register(User insert) {
        userDao.save(insert);
        //用户注册的事件 this表示事件的订阅者需要知道事件是哪个类发出来的
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this, insert));
        return insert.getId();
    }

    /**
     * 用户详情
     * @param uid
     * @return
     */
    @Override
    public UserInfoResp getUserInfo(Long uid) {
        User user = userDao.getById(uid);
        Integer modifyNameCount = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        return UserAdapter.buildUserInfo(user, modifyNameCount);
    }

    /**
     * 修改用户名
     * @param uid
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid") //分布式锁实现
    public void modifyName(Long uid, ModifyNameReq req) {
        //1.查询数据库看你要修改后的这个名字是否已经存在  因为数据库已经保证名字唯一
        User oldUser = userDao.getByName(req.getName());
        AssertUtil.isEmpty(oldUser, "名字已经被抢占了, 请换一个");

        //2.查询最早的没有使用过的一张改名卡 没有改名卡就寄
        UserBackpack modifyNameItem = userBackpackDao.getFirstValidItem(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isNotEmpty(modifyNameItem, "改名次数不够了，等后续活动送改名卡哦");

        //3.使用改名卡 使用乐观锁的方式
        boolean success = userBackpackDao.useItem(modifyNameItem);
        if(success){
            //改名
            userDao.modifyName(uid, req.getName());
            //删缓存
            userCache.userInfoChange(uid);
        }

    }



    /**
     * 查询可选徽章
     * @param uid
     * @return
     */
    @Override
    public List<BadgeResp> badges(Long uid) {
        //1.查询所有徽章
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        //2.查询用户拥有的徽章
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uid, itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList()));
        //3.查询用户佩戴的徽章
        User user = userDao.getById(uid);
        return UserAdapter.buildBadgeResp(itemConfigs, backpacks, user);
    }

    /**
     * 佩戴徽章
     * @param uid
     * @param req
     */
    @Override
    public void wearingBadge(Long uid, WearingBadgeReq req) {
        //确保有这个徽章
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid, req.getBadgeId());
        AssertUtil.isNotEmpty(firstValidItem, "您没有这个徽章哦，快去达成条件获取吧");
        //确保物品类型是徽章
        ItemConfig itemConfig = itemConfigDao.getById(firstValidItem.getItemId());
        AssertUtil.equal(itemConfig.getType(), ItemTypeEnum.BADGE.getType(), "该徽章不可佩戴");
        //佩戴徽章
        userDao.wearingBadge(uid, req.getBadgeId());
        //删除用户缓存
        userCache.userInfoChange(uid);
    }

    /**
     * 拉黑用户
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void black(BlackReq req) {
        //1.添加被拉黑用户的信息进黑名单表 拉黑类型为uid
        Long uid = req.getUid();
        Black user = new Black();
        user.setTarget(uid.toString());
        user.setType(BlackTypeEnum.UID.getType());
        blackDao.save(user);

        //2.添加被拉黑用户的信息进黑名单表 拉黑类型为ip
        User byId = userDao.getById(uid);
        blackIp(Optional.ofNullable(byId.getIpInfo()).map(IpInfo::getCreateIp).orElse(null));
        blackIp(Optional.ofNullable(byId.getIpInfo()).map(IpInfo::getUpdateIp).orElse(null));

        //3.向所有用户发布拉黑的消息
        applicationEventPublisher.publishEvent(new UserBlackEvent(this, byId));
    }

    /**
     * 获取用户汇总信息
     *
     * @param req
     * @return
     */
    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        //需要前端同步的uid
        List<Long> uidList = getNeedSyncUidList(req.getReqList());
        //加载用户信息
        Map<Long, SummeryInfoDTO> batch = userSummaryCache.getBatch(uidList);
        return req.getReqList()
                .stream()
                //需要更新就返回batch中最新查出来的，不需要则只需要设置needfresh字段为false，@JsonInclude.Include.NON_NULL注解会将null的字段不进行序列化
                .map(a -> batch.containsKey(a.getUid()) ? batch.get(a.getUid()) : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {
        List<Long> needSyncUidList = new ArrayList<>();
        List<Long> userModifyTime = userCache.getUserModifyTime(reqList.stream().map(SummeryInfoReq.infoReq::getUid).collect(Collectors.toList()));
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            //(1)前端传过来的lastModifyTime为null，代表没有懒加载过
            //(2)用户信息更新的lastModifyTime>前端传递过来的lastModifyTime
            //这两种情况都要更新
            if (Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())) {
                needSyncUidList.add(infoReq.getUid());
            }
        }
        return needSyncUidList;
    }

    /**
     * 获取用户item汇总信息
     *
     * @param req
     * @return
     */
    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId());
            //前端传过来的lastModifyTime >= 后端查出来的UpdateTime就不需要更新
            if (Objects.nonNull(a.getLastModifyTime()) && a.getLastModifyTime() >= itemConfig.getUpdateTime().getTime()) {
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }


    private void blackIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return;
        }
        try {
            Black user = new Black();
            user.setTarget(ip);
            user.setType(BlackTypeEnum.IP.getType());
            blackDao.save(user);
        } catch (Exception e) {
            log.error("duplicate black ip:{}", ip);
        }
    }
}
