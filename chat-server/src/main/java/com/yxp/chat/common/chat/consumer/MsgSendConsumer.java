package com.yxp.chat.common.chat.consumer;


import com.yxp.chat.common.chat.dao.ContactDao;
import com.yxp.chat.common.chat.dao.MessageDao;
import com.yxp.chat.common.chat.dao.RoomDao;
import com.yxp.chat.common.chat.dao.RoomFriendDao;

import com.yxp.chat.common.chat.domain.entity.Message;
import com.yxp.chat.common.chat.domain.entity.Room;
import com.yxp.chat.common.chat.domain.entity.RoomFriend;
import com.yxp.chat.common.chat.domain.enums.RoomTypeEnum;
import com.yxp.chat.common.chat.domain.vo.response.ChatMessageResp;
import com.yxp.chat.common.chat.service.ChatService;
import com.yxp.chat.common.chat.service.WeChatMsgOperationService;
import com.yxp.chat.common.chat.service.cache.GroupMemberCache;
import com.yxp.chat.common.chat.service.cache.HotRoomCache;
import com.yxp.chat.common.chat.service.cache.RoomCache;
import com.yxp.chat.common.chatai.service.IChatAIService;
import com.yxp.chat.common.common.constant.MQConstant;
import com.yxp.chat.common.common.domain.dto.MsgSendMessageDTO;
import com.yxp.chat.common.user.service.adapter.WSAdapter;
import com.yxp.chat.common.user.service.cache.UserCache;
import com.yxp.chat.common.user.service.impl.PushService;
import com.yxp.chat.common.websocket.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Description: 发送消息更新房间收信箱，并同步给房间成员信箱
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_MSG_GROUP, topic = MQConstant.SEND_MSG_TOPIC)
@Component
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private IChatAIService openAIService;
    @Autowired
    WeChatMsgOperationService weChatMsgOperationService;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private PushService pushService;

    @Override
    public void onMessage(MsgSendMessageDTO dto) {
        Message message = messageDao.getById(dto.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        //获取 发送者信息和消息详情（包含消息标记）
        ChatMessageResp msgResp = chatService.getMsgResp(message, null);
        //所有房间更新房间最新消息（最后一条消息的id 该房间下最后一条消息的时间等）
        roomDao.refreshActiveTime(room.getId(), message.getId(), message.getCreateTime());
        roomCache.delete(room.getId());
        if (room.isHotRoom()) {//热门群聊推送所有在线的人
            //更新热门群聊时间-redis set（key， roomId， last_msg_time）  方便后续的消息聚合 会话列表获取
            hotRoomCache.refreshActiveTime(room.getId(), message.getCreateTime());
            //推送所有人 这里是通过websocket推送给所有channel
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp));
        } else {
            List<Long> memberUidList = new ArrayList<>();
            if (Objects.equals(room.getType(), RoomTypeEnum.GROUP.getType())) {//普通群聊推送所有群成员
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            } else if (Objects.equals(room.getType(), RoomTypeEnum.FRIEND.getType())) {//单聊对象
                //对单人推送
                RoomFriend roomFriend = roomFriendDao.getByRoomId(room.getId());
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            //更新所有群成员的会话时间 相当于更新每个人的收信箱 作为会话聚合用
            contactDao.refreshOrCreateActiveTime(room.getId(), memberUidList, message.getId(), message.getCreateTime());
            //推送房间成员
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp), memberUidList);
        }
    }


}
