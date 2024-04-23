package com.yxp.chat.common.websocket.service;


import com.yxp.chat.common.websocket.domain.vo.resp.WSBaseResp;
import io.netty.channel.Channel;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface WebSocketService {
    void connect(Channel channel);

    void handleLoginReq(Channel channel) throws WxErrorException;

    boolean offline(Channel channel, Optional<Long> uidOptional);

    void scanLoginSuccess(Integer code, Long id);


    void authorize(Channel channel, String token);

    void sendMsgToAll(WSBaseResp<?> msg);

    /**
     * 通知用户扫码成功
     *
     * @param loginCode
     */
    Boolean scanSuccess(Integer loginCode);
    /**
     * 推动消息给所有在线的人
     *
     * @param wsBaseResp 发送的消息体
     * @param skipUid    需要跳过的人
     */
    void sendToAllOnline(WSBaseResp<?> wsBaseResp, Long skipUid);

    /**
     * 推动消息给所有在线的人
     *
     * @param wsBaseResp 发送的消息体
     */
    void sendToAllOnline(WSBaseResp<?> wsBaseResp);

    void sendToUid(WSBaseResp<?> wsBaseResp, Long uid);

    void removed(Channel channel);
}
