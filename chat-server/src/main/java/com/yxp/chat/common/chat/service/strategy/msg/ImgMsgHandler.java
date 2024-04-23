package com.yxp.chat.common.chat.service.strategy.msg;

import com.yxp.chat.common.chat.dao.MessageDao;
import com.yxp.chat.common.chat.domain.entity.Message;
import com.yxp.chat.common.chat.domain.entity.msg.ImgMsgDTO;
import com.yxp.chat.common.chat.domain.entity.msg.MessageExtra;
import com.yxp.chat.common.chat.domain.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:图片消息
 */
@Component
public class ImgMsgHandler extends AbstractMsgHandler<ImgMsgDTO> {
    @Autowired
    private MessageDao messageDao;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.IMG;
    }

    @Override
    public void saveMsg(Message msg, ImgMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setImgMsgDTO(body);
        messageDao.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getImgMsgDTO();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "图片";
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[图片]";
    }
}
