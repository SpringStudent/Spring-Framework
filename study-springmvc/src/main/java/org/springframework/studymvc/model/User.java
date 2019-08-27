package org.springframework.studymvc.model;

import java.io.Serializable;

/**
 * @author 周宁
 * @Date 2019-08-09 9:09
 */
public class User implements Serializable {

    private String username;

    private Integer age;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", age=" + age +
                '}';
    }
}
