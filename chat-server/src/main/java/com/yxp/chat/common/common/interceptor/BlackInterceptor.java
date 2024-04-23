package com.yxp.chat.common.common.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.yxp.chat.common.common.exception.HttpErrorEnum;
import com.yxp.chat.common.common.domain.dto.RequestInfo;
import com.yxp.chat.common.common.utils.RequestHolder;
import com.yxp.chat.common.user.domain.enums.BlackTypeEnum;
import com.yxp.chat.common.user.service.cache.UserCache;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 黑名单用户的拦截器
 */
@Order(2)
@Slf4j
@Component
public class BlackInterceptor implements HandlerInterceptor {

    @Autowired
    private UserCache userCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.查缓存看黑名单 (Black::type, uid)
        Map<Integer, Set<String>> blackMap = userCache.getBlackMap();
        RequestInfo requestInfo = RequestHolder.get();
        //2.如果id在黑名单就禁止访问
        if(inBlackList(requestInfo.getUid(), blackMap.get(BlackTypeEnum.UID.getType()))){
            HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
            return false;
        }
        //3.如果ip在黑名单就禁止访问
        if(inBlackList(requestInfo.getIp(), blackMap.get(BlackTypeEnum.IP.getType()))){
            HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
            return false;
        }
        return true;
    }

    private boolean inBlackList(Object target, Set<String> set) {
        if(Objects.isNull(target) || Collections.isEmpty(set)){
            return false;
        }
        return set.contains(target.toString());
    }


}