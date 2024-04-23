package com.yxp.chat.common.common.event.listener;


import com.yxp.chat.common.websocket.service.adapter.WebSocketAdapter;
import com.yxp.chat.common.common.event.UserBlackEvent;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.service.cache.UserCache;
import com.yxp.chat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户拉黑监听器
 */
@Slf4j
@Component
public class UserBlackListener {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserCache userCache;

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendMsg(UserBlackEvent event){
        User user = event.getUser();
        webSocketService.sendMsgToAll(WebSocketAdapter.buildBlack(user));
    }


    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void changeUserStatus(UserBlackEvent event){
        userDao.inValidUid(event.getUser().getId());
    }

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void evictCache(UserBlackEvent event){
        userCache.evictBlackMap();
        userCache.remove(event.getUser().getId());
    }
}
