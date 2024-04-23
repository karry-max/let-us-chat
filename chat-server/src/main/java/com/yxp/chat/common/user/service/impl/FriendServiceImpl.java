package com.yxp.chat.common.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.yxp.chat.common.common.annotation.RedissonLock;
import com.yxp.chat.common.common.domain.vo.req.CursorPageBaseReq;
import com.yxp.chat.common.common.domain.vo.req.PageBaseReq;
import com.yxp.chat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.yxp.chat.common.common.domain.vo.resp.PageBaseResp;
import com.yxp.chat.common.common.event.UserApplyEvent;
import com.yxp.chat.common.common.utils.AssertUtil;
import com.yxp.chat.common.user.service.adapter.FriendAdapter;
import com.yxp.chat.common.chat.dao.RoomFriendDao;
import com.yxp.chat.common.chat.domain.entity.RoomFriend;
import com.yxp.chat.common.chat.service.ChatService;
import com.yxp.chat.common.chat.service.ContactService;
import com.yxp.chat.common.chat.service.RoomService;
import com.yxp.chat.common.chat.service.adapter.MessageAdapter;
import com.yxp.chat.common.user.dao.UserApplyDao;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.user.dao.UserFriendDao;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.domain.entity.UserApply;
import com.yxp.chat.common.user.domain.entity.UserFriend;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendApplyReq;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendApproveReq;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendCheckReq;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendApplyResp;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendCheckResp;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendResp;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendUnreadResp;
import com.yxp.chat.common.user.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yxp.chat.common.user.domain.enums.ApplyStatusEnum.WAIT_APPROVAL;


/**
 * @author : limeng
 * @description : 好友
 * @date : 2023/07/19
 */
@Slf4j
@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserFriendDao userFriendDao;
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomFriendDao roomFriendDao;

    /**
     * 检查
     * 检查是否是自己好友
     *
     * @param uid     uid
     * @param request 请求
     * @return {@link FriendCheckResp}
     */
    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq request) {
        List<UserFriend> friendList = userFriendDao.getByFriends(uid, request.getUidList());
        //我的好友的set集合
        Set<Long> friendUidSet = friendList.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        List<FriendCheckResp.FriendCheck> friendCheckList = request.getUidList().stream().map(friendUid -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(friendUid);
            friendCheck.setIsFriend(friendUidSet.contains(friendUid));
            return friendCheck;
        }).collect(Collectors.toList());
        return new FriendCheckResp(friendCheckList);
    }

    /**
     * 申请好友
     *
     * @param request 请求
     */
    @Override
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq request) {
        //是否有好友关系
        UserFriend friend = userFriendDao.getByFriend(uid, request.getTargetUid());
        AssertUtil.isEmpty(friend, "你们已经是好友了");
        //是否有待审批的申请记录(自己的)
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, request.getTargetUid());
        if (Objects.nonNull(selfApproving)) {
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, request.getTargetUid());
            return;
        }
        //是否有待审批的申请记录(别人请求自己的)
        UserApply friendApproving = userApplyDao.getFriendApproving(request.getTargetUid(), uid);
        if (Objects.nonNull(friendApproving)) {
            //如果要加的好友给自己发过没有审批的好友申请，那么就直接同意该申请
            ((FriendService) AopContext.currentProxy()).applyApprove(uid, new FriendApproveReq(friendApproving.getId()));
            return;
        }
        //申请入库
        UserApply insert = FriendAdapter.buildFriendApply(uid, request);
        userApplyDao.save(insert);
        //申请事件  给被加的人推送申请消息以及消息未读数
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, insert));
    }

    /**
     * 分页查询好友申请
     *
     * @param request 请求
     * @return {@link PageBaseResp}<{@link FriendApplyResp}>
     */
    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request) {
        IPage<UserApply> userApplyIPage = userApplyDao.friendApplyPage(uid, request.plusPage());
        if (CollectionUtil.isEmpty(userApplyIPage.getRecords())) {
            return PageBaseResp.empty();
        }
        //将这些申请列表设为已读
        readApples(uid, userApplyIPage);
        //返回消息
        return PageBaseResp.init(userApplyIPage, FriendAdapter.buildFriendApplyList(userApplyIPage.getRecords()));
    }

    private void readApples(Long uid, IPage<UserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords()
                .stream().map(UserApply::getId)
                .collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    /**
     * 申请未读数
     *
     * @return {@link FriendUnreadResp}
     */
    @Override
    public FriendUnreadResp unread(Long uid) {
        Integer unReadCount = userApplyDao.getUnReadCount(uid);
        return new FriendUnreadResp(unReadCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq request) {
        UserApply userApply = userApplyDao.getById(request.getApplyId());
        AssertUtil.isNotEmpty(userApply, "不存在申请记录");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), WAIT_APPROVAL.getCode(), "已同意好友申请");
        //同意申请
        userApplyDao.agree(request.getApplyId());
        //创建双方好友关系
        createFriend(uid, userApply.getUid());
        //创建一个聊天房间
        RoomFriend roomFriend = roomService.createFriendRoom(Arrays.asList(uid, userApply.getUid()));
        //发送一条同意消息。。我们已经是好友了，开始聊天吧
        chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()), uid);
    }

    /**
     * 删除好友
     *
     * @param uid       uid
     * @param friendUid 朋友uid
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long uid, Long friendUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, friendUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{},{}", uid, friendUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        //单方面删除好友
        userFriendDao.removeByIds(friendRecordIds);
        //禁用房间
        roomService.disableFriendRoom(Arrays.asList(uid, friendUid));
    }

    /**
     * 好友列表
     * @param uid
     * @param request
     * @return
     */
    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request) {
        //调用游标翻页工具类进行翻页
        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, request);

        if (CollectionUtils.isEmpty(friendPage.getList())) {
            return CursorPageBaseResp.empty();
        }
        //根据friend的uid查询user信息（Id, ActiveStatus, Name, Avatar）
        List<Long> friendUids = friendPage.getList()
                .stream().map(UserFriend::getFriendUid)
                .collect(Collectors.toList());
        List<User> userList = userDao.getFriendList(friendUids);
        //最后封装（activeStatus，uid）返回
        return CursorPageBaseResp.init(friendPage, FriendAdapter.buildFriend(friendPage.getList(), userList));
    }

    private void createFriend(Long uid, Long targetUid) {
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUid(uid);
        userFriend1.setFriendUid(targetUid);
        UserFriend userFriend2 = new UserFriend();
        userFriend2.setUid(targetUid);
        userFriend2.setFriendUid(uid);
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1, userFriend2));
    }

}
