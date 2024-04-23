package com.yxp.chat.common.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.yxp.chat.common.common.annotation.RedissonLock;
import com.yxp.chat.common.common.service.LockService;
import com.yxp.chat.common.common.utils.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Description: 分布式锁切面
 */
@Slf4j
@Aspect
@Component
@Order(0)//确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {

    @Autowired
    private LockService lockService;

    @Around("@annotation(com.yxp.chat.common.common.annotation.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        //前缀默认为方法的全限定名
        String prefix = StrUtil.isBlank(redissonLock.prefixKey()) ? SpElUtils.getMethodKey(method) : redissonLock.prefixKey();

        //SpEL解析出注解中的key的值 比如（key=“#uid”）解析出具体的uid值作为key的值，最后的key包含前缀
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());
        return lockService.executeWithLockThrows(prefix + ":" + key, redissonLock.waitTime(), redissonLock.unit(), joinPoint::proceed);
    }
}
