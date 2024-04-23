package com.yxp;

import com.yxp.chat.common.ChatApplication;
import com.yxp.chat.common.user.domain.entity.User;
import com.yxp.chat.common.user.domain.enums.IdempotentEnum;
import com.yxp.chat.common.user.domain.enums.ItemEnum;
import com.yxp.chat.common.user.domain.vo.req.ModifyNameReq;
import com.yxp.chat.common.user.mapper.UserMapper;

import com.yxp.chat.common.user.service.UserService;
import com.yxp.chat.common.user.service.impl.UserBackpackServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {ChatApplication.class})
@RunWith(SpringRunner.class)
public class DaoTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBackpackServiceImpl userBackpackService;

    @Autowired
    private UserService userService;

    @Test
    public void testUserDao(){
        User user = userMapper.selectById(10002);
        System.out.println(user);
    }

    @Test
    public void acquireItem(){
        userBackpackService.acquireItem(20000L, ItemEnum.PLANET.getId(), IdempotentEnum.UID, "20000");
    }

    @Test
    public void testRedissonLock(){
        userService.modifyName(1L, new ModifyNameReq("xiao"));
    }
}
