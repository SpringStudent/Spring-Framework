package org.springframework.study.day14.jdkproxy;

/**
 * @author 周宁
 * @Date 2019-07-25 14:27
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public void sayHello() {
        System.out.println("hello java");
    }

    @Override
    public void gun() {
        System.out.println("gun");
    }
}
