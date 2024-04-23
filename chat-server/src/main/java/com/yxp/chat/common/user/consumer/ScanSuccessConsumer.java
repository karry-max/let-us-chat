package com.yxp.chat.common.user.consumer;


import com.yxp.chat.common.common.constant.MQConstant;
import com.yxp.chat.common.common.domain.dto.ScanSuccessMessageDTO;
import com.yxp.chat.common.websocket.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description: 将扫码成功的信息发送给对应的用户,等待授权
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SCAN_MSG_GROUP, topic = MQConstant.SCAN_MSG_TOPIC, messageModel = MessageModel.BROADCASTING)
@Component
public class ScanSuccessConsumer implements RocketMQListener<ScanSuccessMessageDTO> {
    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void onMessage(ScanSuccessMessageDTO scanSuccessMessageDTO) {
        webSocketService.scanSuccess(scanSuccessMessageDTO.getCode());
    }

}
