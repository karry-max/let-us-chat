package com.yxp.chat.common.common.event.listener;

import com.yxp.chat.common.common.event.UserOnlineEvent;
import com.yxp.chat.common.user.dao.UserDao;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.domain.enums.ChatActiveStatusEnum;
import com.yxp.chat.common.user.service.IUserBackpackService;
import com.yxp.chat.common.user.service.IpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户上线监听器
 */
@Slf4j
@Component
public class UserOnlineListener {
    @Autowired
    private UserDao userDao;
    @Autowired
    private IUserBackpackService iUserBackpackService;

    @Autowired
    private IpService ipService;

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveDB(UserOnlineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastOptTime(user.getLastOptTime());
        update.setIpInfo(user.getIpInfo());
        update.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
        userDao.updateById(update);

        //更新用户ip详情
        ipService.refreshIpDetailAsync(user.getId());
    }

}
