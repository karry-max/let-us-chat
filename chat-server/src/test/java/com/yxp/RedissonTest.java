package com.yxp;

import com.yxp.chat.common.ChatApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void redisson() {
        RLock lock = redissonClient.getLock("123");
        lock.lock();
        System.out.println();
        lock.unlock();
    }
}
