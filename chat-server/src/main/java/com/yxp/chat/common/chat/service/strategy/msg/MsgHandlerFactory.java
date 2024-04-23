package com.yxp.chat.common.chat.service.strategy.msg;


import com.yxp.chat.common.common.exception.CommonErrorEnum;
import com.yxp.chat.common.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: 消息处理器工厂
 */
public class MsgHandlerFactory {
    // 不同的消息类型对应不同的消息处理器
    private static final Map<Integer, AbstractMsgHandler> STRATEGY_MAP = new HashMap<>();

    public static void register(Integer code, AbstractMsgHandler strategy) {
        STRATEGY_MAP.put(code, strategy);
    }

    public static AbstractMsgHandler getStrategyNoNull(Integer code) {
        AbstractMsgHandler strategy = STRATEGY_MAP.get(code);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_VALID);
        return strategy;
    }
}
