package com.yxp.chat.common.user.service.impl;

import com.yxp.chat.common.common.annotation.RedissonLock;
import com.yxp.chat.common.common.domain.enums.YesOrNoEnum;
import com.yxp.chat.common.common.event.ItemReceiveEvent;
import com.yxp.chat.common.user.dao.UserBackpackDao;
import com.yxp.chat.common.user.domain.entity.UserBackpack;
import com.yxp.chat.common.user.domain.enums.IdempotentEnum;
import com.yxp.chat.common.user.service.IUserBackpackService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserBackpackServiceImpl implements IUserBackpackService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    @Lazy
    private UserBackpackServiceImpl userBackpackService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    /**
     * 用户获取一个物品
     *
     * @param uid            用户id
     * @param itemId         物品id
     * @param idempotentEnum 幂等类型
     * @param businessId     上层业务发送的唯一标识
     */
    @Override
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        //构造幂等
        String idempotent = getIdempotent(itemId, idempotentEnum, businessId);

        //使用分布式锁
        //为了防止同类调用，注解不起作用，必须使用spring容器注入,还可以使用代理的方法
        userBackpackService.doAcquireItem(uid, itemId, idempotent);

    }

    @RedissonLock(key = "#idempotent", waitTime = 5000)
    public void doAcquireItem(Long uid, Long itemId, String idempotent){
        //查询是否有该幂等 也就是是否已经发放
        UserBackpack userBackpack = userBackpackDao.getByIdempotent(idempotent);
        if(Objects.nonNull(userBackpack)){ //已经发放了直接返回
            return;
        }
        //没有则发放
        UserBackpack insert = UserBackpack.builder()
                .uid(uid)
                .itemId(itemId)
                .status(YesOrNoEnum.YES.getStatus())
                .idempotent(idempotent)
                .build();
        userBackpackDao.save(insert);
        //用户收到物品的事件
        applicationEventPublisher.publishEvent(new ItemReceiveEvent(this, insert));
    }

    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        return String.format("%d_%d_%s", itemId, idempotentEnum.getType(), businessId);
    }
}
