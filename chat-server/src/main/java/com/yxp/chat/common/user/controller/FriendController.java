package com.yxp.chat.common.user.controller;



import com.yxp.chat.common.common.domain.vo.req.CursorPageBaseReq;
import com.yxp.chat.common.common.domain.vo.req.PageBaseReq;
import com.yxp.chat.common.common.domain.vo.resp.ApiResult;
import com.yxp.chat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.yxp.chat.common.common.domain.vo.resp.PageBaseResp;
import com.yxp.chat.common.common.utils.RequestHolder;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendApplyReq;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendApproveReq;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendCheckReq;
import com.yxp.chat.common.user.domain.vo.req.friend.FriendDeleteReq;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendApplyResp;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendCheckResp;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendResp;
import com.yxp.chat.common.user.domain.vo.resp.friend.FriendUnreadResp;
import com.yxp.chat.common.user.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 好友相关接口
 */
@RestController
@RequestMapping("/capi/user/friend")
@Api(tags = "好友相关接口")
@Slf4j
public class FriendController {
    @Resource
    private FriendService friendService;

    @GetMapping("/check")
    @ApiOperation("批量判断是否是自己好友")
    public ApiResult<FriendCheckResp> check(@Valid FriendCheckReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.check(uid, request));
    }

    /**
     * 发送申请
     * @param request
     * @return
     */
    @PostMapping("/apply")
    @ApiOperation("申请好友")
    public ApiResult<Void> apply(@Valid @RequestBody FriendApplyReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.apply(uid, request);
        return ApiResult.success();
    }

    /**
     * 单方面从好友列表删除 roomfriend设置为禁用状态
     * @param request
     * @return
     */
    @DeleteMapping()
    @ApiOperation("删除好友")
    public ApiResult<Void> delete(@Valid @RequestBody FriendDeleteReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.deleteFriend(uid, request.getTargetUid());
        return ApiResult.success();
    }

    /**
     * 普通分页
     * @param request
     * @return
     */
    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public ApiResult<PageBaseResp<FriendApplyResp>> page(@Valid PageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.pageApplyFriend(uid, request));
    }

    @GetMapping("/apply/unread")
    @ApiOperation("申请未读数")
    public ApiResult<FriendUnreadResp> unread() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.unread(uid));
    }

    /**
     * 同意好友申请
     * @param request
     * @return
     */
    @PutMapping("/apply")
    @ApiOperation("审批同意")
    public ApiResult<Void> applyApprove(@Valid @RequestBody FriendApproveReq request) {
        friendService.applyApprove(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    /**
     * 游标分页
     * @param request
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("联系人列表")
    public ApiResult<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.friendList(uid, request));
    }
}

