package com.yxp.chat.common.chat.controller;


import com.yxp.chat.common.common.domain.vo.req.CursorPageBaseReq;
import com.yxp.chat.common.common.domain.vo.req.IdReqVO;
import com.yxp.chat.common.common.domain.vo.resp.ApiResult;
import com.yxp.chat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.yxp.chat.common.common.utils.RequestHolder;
import com.yxp.chat.common.chat.domain.vo.request.ContactFriendReq;
import com.yxp.chat.common.chat.domain.vo.response.ChatRoomResp;
import com.yxp.chat.common.chat.service.ChatService;
import com.yxp.chat.common.chat.service.RoomAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 会话相关接口
 */
@RestController
@RequestMapping("/capi/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ContactController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private RoomAppService roomService;

    /**
     * 会话列表的展示 按最新消息的时间排序 并且还要加载会话的详情（房间（好友）头像、最新消息、房间名称等）
     * @param request
     * @return
     */
    @GetMapping("/public/contact/page")
    @ApiOperation("会话列表")
    public ApiResult<CursorPageBaseResp<ChatRoomResp>> getRoomPage(@Valid CursorPageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getContactPage(request, uid));
    }

    /**
     * 新消息来时 加载会话详情(原先会话列表中没有该会话)
     * @param request(roomId)
     * @return
     */
    @GetMapping("/public/contact/detail")
    @ApiOperation("会话详情")
    public ApiResult<ChatRoomResp> getContactDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getContactDetail(uid, request.getId()));
    }

    /**
     * 给好友发信息时 确定会话房间 加载会话详情
     * @param request
     * @return
     */
    @GetMapping("/public/contact/detail/friend")
    @ApiOperation("会话详情(联系人列表发消息用)")
    public ApiResult<ChatRoomResp> getContactDetailByFriend(@Valid ContactFriendReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getContactDetailByFriend(uid, request.getUid()));
    }
}

