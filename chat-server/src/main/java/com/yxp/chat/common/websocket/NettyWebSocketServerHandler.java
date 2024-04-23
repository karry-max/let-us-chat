package com.yxp.chat.common.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.yxp.chat.common.websocket.domain.enums.WSReqTypeEnum;
import com.yxp.chat.common.websocket.domain.vo.req.WSBaseReq;
import com.yxp.chat.common.websocket.service.WebSocketService;
import io.github.classgraph.json.JSONUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable                     //SimpleChannelInboundHandler<TextWebSocketFrame>根据消息的类型进行选择处理，也就是消息类型为TextWebSocketFrame才处理
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;

    /**
     * WebSocket连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        webSocketService = SpringUtil.getBean(WebSocketService.class);
        webSocketService.connect(ctx.channel());
    }

    /**
     * 客户端主动下线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOffline(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            this.webSocketService.connect(ctx.channel());
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            if (StrUtil.isNotBlank(token)) {
                this.webSocketService.authorize(ctx.channel(), token);
            }
            System.out.println("握手完成!");
        }else if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("读空闲");
                //读空闲时用户下线
                userOffline(ctx.channel());
            }
        }
    }

    /**
     * 用户下线统一处理
     * @param channel
     */
    private void userOffline(Channel channel){
        webSocketService.removed(channel);
        channel.close();
    }

    // 读取客户端发送的请求报文
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //获取前端传过来的text
        String text = msg.text();
        //解析
        WSBaseReq wsBaseReq = JSONUtil.toBean(text, WSBaseReq.class);
        switch (WSReqTypeEnum.of(wsBaseReq.getType())) {
            case AUTHORIZE:
                //webSocketService.authorize(ctx.channel(), wsBaseReq.getData());
                break;
            case HEARTBEAT:
                break;
            case LOGIN:
                webSocketService.handleLoginReq(ctx.channel());
        }
    }
}
