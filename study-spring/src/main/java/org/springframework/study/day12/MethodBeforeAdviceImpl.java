package org.springframework.study.day12;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @author 周宁
 * @Date 2019-07-22 16:34
 */
public class MethodBeforeAdviceImpl implements MethodBeforeAdvice {
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("before advice" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("arguments" + (i + 1) + "：" + args[i]);
            }
        }
        System.out.println("target:" + target.toString());
    }
}
