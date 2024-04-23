package com.yxp.chat.common.chat.service.cache;

import com.yxp.chat.common.user.dao.BlackDao;
import com.yxp.chat.common.user.dao.RoleDao;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.chat.dao.MessageDao;
import com.yxp.chat.common.chat.domain.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Description: 消息相关缓存
 */
@Component
public class MsgCache {

    @Autowired
    private UserDao userDao;
    @Autowired
    private BlackDao blackDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private MessageDao messageDao;

    @Cacheable(cacheNames = "msg", key = "'msg'+#msgId")
    public Message getMsg(Long msgId) {
        return messageDao.getById(msgId);
    }

    @CacheEvict(cacheNames = "msg", key = "'msg'+#msgId")
    public Message evictMsg(Long msgId) {
        return null;
    }
}
