package com.yxp.chat.common.chat.service.strategy.mark;

import com.yxp.chat.common.chat.domain.enums.MessageMarkTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Description: 点赞标记策略类
 */
@Component
public class LikeStrategy extends AbstractMsgMarkStrategy {

    @Override
    protected MessageMarkTypeEnum getTypeEnum() {
        return MessageMarkTypeEnum.LIKE;
    }

    @Override
    public void doMark(Long uid, Long msgId) {
        super.doMark(uid, msgId);
        //同时取消点踩的动作
        MsgMarkFactory.getStrategyNoNull(MessageMarkTypeEnum.DISLIKE.getType()).unMark(uid, msgId);
    }
}
