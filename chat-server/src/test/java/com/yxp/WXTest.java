package com.yxp;

import com.yxp.chat.common.ChatApplication;
import com.yxp.chat.transaction.service.MQProducer;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class WXTest {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private MQProducer mqProducer;

    @Test
    public void test() throws WxErrorException {
        WxMpQrcodeService qrcodeService = wxMpService.getQrcodeService();
        WxMpQrCodeTicket wxMpQrCodeTicket = qrcodeService.qrCodeCreateTmpTicket(1, 1);
        System.out.println(wxMpQrCodeTicket.getUrl());

    }

    @Test
    public void sendMQ() {
        Message<String> build = MessageBuilder.withPayload("123").build();
        mqProducer.sendSecureMsg("test-topic", build, "123");
    }
}
