package com.yxp;

import com.yxp.chat.common.ChatApplication;
import com.yxp.chat.common.common.utils.JwtUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class JwtTest {


    @Autowired
    private JwtUtils jwtUtils;

    @Test
    public void test01(){
        System.out.println(jwtUtils.createToken(1L));
        System.out.println(jwtUtils.createToken(1L));
    }
}
