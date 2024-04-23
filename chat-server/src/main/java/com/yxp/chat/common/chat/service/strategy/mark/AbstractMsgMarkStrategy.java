package com.yxp.chat.common.chat.service.strategy.mark;
import com.yxp.chat.common.common.domain.enums.YesOrNoEnum;
import com.yxp.chat.common.common.event.MessageMarkEvent;
import com.yxp.chat.common.common.exception.BusinessException;
import com.yxp.chat.common.chat.dao.MessageMarkDao;
import com.yxp.chat.common.chat.domain.dto.ChatMessageMarkDTO;
import com.yxp.chat.common.chat.domain.entity.MessageMark;
import com.yxp.chat.common.chat.domain.enums.MessageMarkActTypeEnum;
import com.yxp.chat.common.chat.domain.enums.MessageMarkTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;

/**
 * Description: 消息标记抽象类
 */
public abstract class AbstractMsgMarkStrategy {
    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    protected abstract MessageMarkTypeEnum getTypeEnum();

    @Transactional
    public void mark(Long uid, Long msgId) {
        doMark(uid, msgId);
    }

    @Transactional
    public void unMark(Long uid, Long msgId) {
        doUnMark(uid, msgId);
    }

    @PostConstruct
    private void init() {
        MsgMarkFactory.register(getTypeEnum().getType(), this);
    }

    protected void doMark(Long uid, Long msgId) {
        exec(uid, msgId, MessageMarkActTypeEnum.MARK);
    }

    protected void doUnMark(Long uid, Long msgId) {
        exec(uid, msgId, MessageMarkActTypeEnum.UN_MARK);
    }

    /**
     * 修改数据库的msgId的状态为（点赞，确认）或（点赞，取消）或（点踩，确认）或（点踩，取消）
     * @param uid
     * @param msgId
     * @param actTypeEnum
     */
    protected void exec(Long uid, Long msgId, MessageMarkActTypeEnum actTypeEnum) {

        Integer markType = getTypeEnum().getType();
        Integer actType = actTypeEnum.getType();
        MessageMark oldMark = messageMarkDao.get(uid, msgId, markType);
        //如果前端传递过来的是取消类型，那么说明前面已经确认过，所以数据库一定会有数据，但是现在为null
        if (Objects.isNull(oldMark) && actTypeEnum == MessageMarkActTypeEnum.UN_MARK) {
            //取消的类型，数据库一定有记录，没有就直接跳过操作
            return;
        }
        //插入一条新消息,或者修改一条消息
        MessageMark insertOrUpdate = MessageMark.builder()
                .id(Optional.ofNullable(oldMark).map(MessageMark::getId).orElse(null))
                .uid(uid)
                .msgId(msgId)
                .type(markType)
                .status(transformAct(actType))
                .build();
        boolean modify = messageMarkDao.saveOrUpdate(insertOrUpdate);
        if (modify) {
            //修改成功才发布消息标记事件
            ChatMessageMarkDTO dto = new ChatMessageMarkDTO(uid, msgId, markType, actType);
            applicationEventPublisher.publishEvent(new MessageMarkEvent(this, dto));
        }
    }

    private Integer transformAct(Integer actType) {
        if (actType == 1) {
            return YesOrNoEnum.NO.getStatus();
        } else if (actType == 2) {
            return YesOrNoEnum.YES.getStatus();
        }
        throw new BusinessException("动作类型 1确认 2取消");
    }

}
