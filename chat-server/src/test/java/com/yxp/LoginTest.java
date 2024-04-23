package com.yxp;

import com.yxp.chat.common.ChatApplication;
import com.yxp.chat.common.user.service.LoginService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class LoginTest {


    @Autowired
    private LoginService loginService;

    @Test
    public void test01(){
        String token = loginService.login(10004L);
        //eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEwMDA0LCJjcmVhdGVUaW1lIjoxNzExNzI2ODk0fQ.W5gob0HK-ut-HIagOH-JZydmNu0yPrepFQ2YBCHUtJE
        System.out.println(token);
    }
}
