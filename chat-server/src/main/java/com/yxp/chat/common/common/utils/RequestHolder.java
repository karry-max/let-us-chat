package com.yxp.chat.common.common.utils;


import com.yxp.chat.common.common.domain.dto.RequestInfo;

public class RequestHolder {
    private static final ThreadLocal<RequestInfo> threadLocal = new ThreadLocal<>();

    public static void set(RequestInfo requestInfo){
        threadLocal.set(requestInfo);
    }

    public static RequestInfo get(){
        return threadLocal.get();
    }

    public static void remove(){
        threadLocal.remove();
    }
}
