package org.springframework.study.day12;

import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;

/**
 * @author 周宁
 * @Date 2019-07-22 16:39
 */
public class ThrowAdviceImpl implements ThrowsAdvice  {

    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
        System.out.println("after throwing" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("arguments" + (i + 1) + "：" + args[i]);
            }
        }
        System.out.println("target:" + target.toString());
    }

}
