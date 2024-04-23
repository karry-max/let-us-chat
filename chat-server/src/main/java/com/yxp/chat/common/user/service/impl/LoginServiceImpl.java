package com.yxp.chat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yxp.chat.common.common.utils.JwtUtils;
import com.yxp.chat.common.common.utils.RedisUtils;
import com.yxp.chat.common.common.constant.RedisKey;
import com.yxp.chat.common.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    public static final int TOKEN_EXPIRE_DAYS = 3;
    public static final int TOKEN_RENEWAL_DAYS = 1;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 刷新token有效期
     *
     * @param token
     */
    @Override
    @Async  //线程池实现异步（自定义线程池）
    public void renewalTokenIfNecessary(String token) {
        Long uid = getValidUid(token);
        String userTokenKey = getUserTokenKey(uid);
        Long expireDays = RedisUtils.getExpire(userTokenKey, TimeUnit.DAYS);
        if(expireDays == -2){ //不存在的key
            return;
        }
        if(expireDays < TOKEN_RENEWAL_DAYS) {//小于1刷新有效期
            RedisUtils.expire(getUserTokenKey(uid), TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        }
    }

    /**
     * 登录成功，获取token
     *
     * @param uid
     * @return 返回token
     */
    @Override
    public String login(Long uid) {
        String token = jwtUtils.createToken(uid);
        RedisUtils.set(getUserTokenKey(uid), token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        return token;
    }

    /**
     * 如果token有效，返回uid
     *（1）能不能解析出uid （2）有没有失效
     * @param token
     * @return
     */
    @Override
    public Long getValidUid(String token) {
        Long uid = jwtUtils.getUidOrNull(token);
        if(Objects.isNull(uid)){
            return null;
        }
        String oldToken = RedisUtils.getStr(getUserTokenKey(uid));
        return Objects.equals(oldToken, token) ? uid : null;
    }

    private String getUserTokenKey(Long uid){
        return RedisKey.getKey(RedisKey.USER_TOKEN_STRING, uid);
    }
}
