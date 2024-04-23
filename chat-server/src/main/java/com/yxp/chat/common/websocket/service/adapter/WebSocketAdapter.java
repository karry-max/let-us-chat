package com.yxp.chat.common.websocket.service.adapter;

import com.yxp.chat.common.websocket.domain.vo.resp.WSBlack;
import com.yxp.chat.common.websocket.domain.vo.resp.WSLoginUrl;
import com.yxp.chat.common.common.domain.enums.YesOrNoEnum;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.websocket.domain.enums.WSRespTypeEnum;
import com.yxp.chat.common.websocket.domain.vo.resp.WSBaseResp;
import com.yxp.chat.common.websocket.domain.vo.resp.WSLoginSuccess;
import lombok.val;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

public class WebSocketAdapter {
    public static WSBaseResp<?> buildResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
        WSBaseResp<WSLoginUrl> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        resp.setData(new WSLoginUrl(wxMpQrCodeTicket.getUrl()));
        return resp;
    }

    public static WSBaseResp<?> buildResp(User user, String token, boolean power) {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        WSLoginSuccess build = WSLoginSuccess.builder()
                .avatar(user.getAvatar())
                .name(user.getName())
                .token(token)
                .uid(user.getId())
                .power(power ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus())
                .build();
        resp.setData(build);
        return resp;
    }


    public static WSBaseResp<?> buildWaitAuthorizeResp() {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
        return resp;
    }

    public static WSBaseResp<?> buildInvalidTokenResp() {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.INVALIDATE_TOKEN.getType());
        return resp;
    }

    public static WSBaseResp<?> buildBlack(User user) {
        WSBaseResp<WSBlack> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        WSBlack build = WSBlack.builder()
                .uid(user.getId())
                .build();
        resp.setData(build);
        return resp;
    }
}
