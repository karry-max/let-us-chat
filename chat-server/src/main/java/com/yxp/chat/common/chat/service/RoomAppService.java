package com.yxp.chat.common.chat.service;


import com.yxp.chat.common.common.domain.vo.req.CursorPageBaseReq;
import com.yxp.chat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.yxp.chat.common.websocket.domain.vo.resp.ChatMemberResp;
import com.yxp.chat.common.chat.domain.vo.request.ChatMessageMemberReq;
import com.yxp.chat.common.chat.domain.vo.request.GroupAddReq;
import com.yxp.chat.common.chat.domain.vo.request.member.MemberAddReq;
import com.yxp.chat.common.chat.domain.vo.request.member.MemberDelReq;
import com.yxp.chat.common.chat.domain.vo.request.member.MemberReq;
import com.yxp.chat.common.chat.domain.vo.response.ChatMemberListResp;
import com.yxp.chat.common.chat.domain.vo.response.ChatRoomResp;
import com.yxp.chat.common.chat.domain.vo.response.MemberResp;

import java.util.List;

/**
 * Description:
 */
public interface RoomAppService {
    /**
     * 获取会话列表--支持未登录态
     */
    CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid);

    /**
     * 获取群组信息
     */
    MemberResp getGroupDetail(Long uid, long roomId);

    CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request);

    List<ChatMemberListResp> getMemberList(ChatMessageMemberReq request);

    void delMember(Long uid, MemberDelReq request);

    void addMember(Long uid, MemberAddReq request);

    Long addGroup(Long uid, GroupAddReq request);

    ChatRoomResp getContactDetail(Long uid, Long roomId);

    ChatRoomResp getContactDetailByFriend(Long uid, Long friendUid);
}
