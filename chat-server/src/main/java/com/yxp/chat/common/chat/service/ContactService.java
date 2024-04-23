package com.yxp.chat.common.chat.service;


import com.yxp.chat.common.chat.domain.dto.MsgReadInfoDTO;
import com.yxp.chat.common.chat.domain.entity.Contact;
import com.yxp.chat.common.chat.domain.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 会话列表 服务类
 */
public interface ContactService {
    /**
     * 创建会话
     */
    Contact createContact(Long uid, Long roomId);

    Integer getMsgReadCount(Message message);

    Integer getMsgUnReadCount(Message message);

    Map<Long, MsgReadInfoDTO> getMsgReadInfo(List<Message> messages);
}
