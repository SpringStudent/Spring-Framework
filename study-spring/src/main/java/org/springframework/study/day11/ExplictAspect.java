package org.springframework.study.day11;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author 周宁
 * @Date 2019-07-22 20:54
 */
public class ExplictAspect {

    public void beforeAdvice(){
        System.out.println("before advice");
    }

    public void afterAdvice(){
        System.out.println("after advice");
    }

    public Object aroundAdvice(ProceedingJoinPoint p) throws Throwable {
        System.out.println("arround-before");
        Object o = null;
        o = p.proceed();
        System.out.println("arround-after");
        return o;
    }
}
