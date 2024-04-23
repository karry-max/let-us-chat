package com.yxp;

import com.yxp.chat.common.ChatApplication;
import com.yxp.chat.common.common.utils.RedisUtils;
import com.yxp.chat.common.user.service.LoginService;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LoginService loginService;

    @Test
    public void redis() {
        redisTemplate.opsForValue().set("name","卷心菜");
        String name = (String) redisTemplate.opsForValue().get("name");
        System.out.println(name); //卷心菜
    }

    @Test
    public void redis02() {
        RedisUtils.set("name","卷心菜");
        String name = RedisUtils.getStr("name");
        System.out.println(name); //卷心菜
    }

    @Test
    public void redis03(){
        String s ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjExMDAyLCJjcmVhdGVUaW1lIjoxNzA5MjEwNjE0fQ.xse79N9yS10OCvGGx7dL710dFx5gNyXamzgB3J43DGs";
        Long validUid = loginService.getValidUid(s);
        System.out.println(validUid);
    }
}
