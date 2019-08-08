package org.springframework.study.day02.constructor;

import java.beans.ConstructorProperties;
import java.util.List;

/**
 * @author 周宁
 * @Date 2019-07-05 16:55
 */
public class Person {

    private String name;

    private int age;

    private List<String> list;
    @ConstructorProperties({"a","b","c"})
    public Person(String name, int age,List<String> list) {
        this.name = name;
        this.age = age;
        this.list = list;
    }

    public Person() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getList() {
        return list;
    }
}
