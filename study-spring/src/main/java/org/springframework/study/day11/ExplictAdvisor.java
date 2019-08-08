package org.springframework.study.day11;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @author 周宁
 * @Date 2019-07-23 14:11
 */
public class ExplictAdvisor implements MethodBeforeAdvice, AfterReturningAdvice, MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("arround-before");
        Object o = null;
        o = invocation.proceed();
        System.out.println("arround-after");
        return o;
    }

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("before advice");
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("after advice");
    }
}
