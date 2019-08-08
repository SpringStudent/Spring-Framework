package org.springframework.study.day14.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author 周宁
 * @Date 2019-07-25 14:28
 */
public class HelloInvocationHandler implements InvocationHandler {

    private Object object;

    public HelloInvocationHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("----start----");
        Object result = method.invoke(object,args);

        System.out.println("-----end------");

        return result;
    }

    public Object getProxy(){
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),object.getClass().getInterfaces(),this);
    }
}
