package com.yxp.chat.common.common.event.listener;

import com.yxp.chat.common.websocket.domain.vo.resp.WSMemberChange;
import com.yxp.chat.common.chat.dao.GroupMemberDao;
import com.yxp.chat.common.chat.domain.entity.GroupMember;
import com.yxp.chat.common.chat.domain.entity.RoomGroup;
import com.yxp.chat.common.chat.domain.vo.request.ChatMessageReq;
import com.yxp.chat.common.chat.service.ChatService;
import com.yxp.chat.common.chat.service.adapter.MemberAdapter;
import com.yxp.chat.common.chat.service.adapter.RoomAdapter;
import com.yxp.chat.common.chat.service.cache.GroupMemberCache;
import com.yxp.chat.common.chat.service.cache.MsgCache;
import com.yxp.chat.common.common.event.GroupMemberAddEvent;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.user.domain.entity.User;

import com.yxp.chat.common.user.service.cache.UserInfoCache;
import com.yxp.chat.common.user.service.impl.PushService;
import com.yxp.chat.common.websocket.domain.vo.resp.WSBaseResp;
import com.yxp.chat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 添加群成员监听器
 */
@Slf4j
@Component
public class GroupMemberAddListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;

    /**
     * 推送邀请消息
     * @param event
     */
    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        Long inviteUid = event.getInviteUid();
        User user = userInfoCache.get(inviteUid);
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        ChatMessageReq chatMessageReq = RoomAdapter.buildGroupAddMessage(roomGroup, user, userInfoCache.getBatch(uidList));
        chatService.sendMsg(chatMessageReq, User.UID_SYSTEM);
    }

    /**
     * 推送添加好友的MQ消息
     * @param event
     */
    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberAddWS(roomGroup.getRoomId(), user);
            //给房间内所有成员推送MQ消息
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
    }

}
