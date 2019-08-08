package org.springframework.study.day12;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author 周宁
 * @Date 2019-07-22 16:42
 */
public class MethodInterceptorImpl implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("invoke start" + invocation.getMethod().getName());
        Object[] args = invocation.getArguments();
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("arguments" + (i + 1) + "：" + args[i]);
            }
        }
        Object proceed = invocation.proceed();
        System.out.println("invoke end");
        return proceed;
    }
}
