package org.springframework.study.day05;

/**
 * @author 周宁
 * @Date 2019-07-10 15:07
 */
public class CreateBeanFactory {

    public static FactoryBean createBean(String name){
        return new FactoryBean(name);
    }
}
