package org.springframework.study.day11;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

/**
 * @author 周宁
 * @Date 2019-07-20 13:50
 */
@Aspect
public class AspectJTest {

    @Pointcut("execution(* *.aspectTest(..))")
    public void p1(){

    }

    @Before("p1()")
    public void before(){
        System.out.println("before");
    }

    @After("p1()")
    public void after(){
        System.out.println("after");
    }

    @Around("p1()")
    public Object arroundTest(ProceedingJoinPoint p) throws Throwable {
        System.out.println("arround-before");
        Object o = null;
        o = p.proceed();
        System.out.println("arround-after");
        return o;
    }

}
