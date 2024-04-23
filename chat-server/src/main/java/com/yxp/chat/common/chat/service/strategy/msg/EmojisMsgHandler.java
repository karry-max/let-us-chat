package com.yxp.chat.common.chat.service.strategy.msg;

import com.yxp.chat.common.chat.dao.MessageDao;
import com.yxp.chat.common.chat.domain.entity.Message;
import com.yxp.chat.common.chat.domain.entity.msg.EmojisMsgDTO;
import com.yxp.chat.common.chat.domain.entity.msg.MessageExtra;
import com.yxp.chat.common.chat.domain.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:表情消息
 */
@Component
public class EmojisMsgHandler extends AbstractMsgHandler<EmojisMsgDTO> {
    @Autowired
    private MessageDao messageDao;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.EMOJI;
    }

    @Override
    public void saveMsg(Message msg, EmojisMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setEmojisMsgDTO(body);
        messageDao.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getEmojisMsgDTO();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "表情";
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[表情包]";
    }
}
