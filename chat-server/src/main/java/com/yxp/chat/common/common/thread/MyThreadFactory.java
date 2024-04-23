package com.yxp.chat.common.common.thread;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

@Slf4j
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {

    private final ThreadFactory factory; //装饰器模式，定义父类的对象，这样只需要完成自己的部分，而父类的部分不需要再重新实现

    @Override
    public Thread newThread(Runnable r) {
        //执行spring线程自己的创建逻辑
        Thread thread = factory.newThread(r);
        //额外装饰我们需要的逻辑
        thread.setUncaughtExceptionHandler(GlobalUncaughtExceptionHandler.getInstance());
        return thread;
    }
}
