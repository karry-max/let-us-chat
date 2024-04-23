package com.yxp.chat.common.common.utils;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.naming.Context;
import java.lang.reflect.Method;
import java.net.ContentHandler;
import java.util.Optional;

/**
 * Description: spring el表达式解析
 */
public class SpElUtils {
    private static final ExpressionParser parser = new SpelExpressionParser();
    //获取方法的实际参数名
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public static String parseSpEl(Method method, Object[] args, String spEl) {
        //获取方法的实际参数名
        String[] params = Optional.ofNullable(parameterNameDiscoverer.getParameterNames(method)).orElse(new String[]{});//解析参数名

        EvaluationContext context = new StandardEvaluationContext();//el解析需要的上下文对象
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);//所有参数都作为原材料扔进去
        }
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }

    /**
     * 获取方法的全限定名
     * @param method
     * @return
     */
    public static String getMethodKey(Method method) {
        return method.getDeclaringClass() + "#" + method.getName();
    }
}
