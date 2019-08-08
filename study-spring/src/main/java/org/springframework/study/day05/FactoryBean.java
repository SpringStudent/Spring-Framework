package org.springframework.study.day05;

/**
 * @author 周宁
 * @Date 2019-07-10 15:06
 */
public class FactoryBean {

    private String name;

    public FactoryBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
