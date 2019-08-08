package org.springframework.study.day12;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * @author 周宁
 * @Date 2019-07-22 16:38
 */
public class MethodAfterReturningImpl implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("after advice" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("arguments" + (i + 1) + "：" + args[i]);
            }
        }
        System.out.println("target:" + target.toString());
    }
}
