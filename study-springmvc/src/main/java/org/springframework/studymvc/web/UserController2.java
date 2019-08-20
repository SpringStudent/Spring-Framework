package org.springframework.studymvc.web;

import org.springframework.studymvc.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周宁
 * @Date 2019-08-19 9:37
 */
@RestController
@RequestMapping("/user")
public class UserController2 {

    @GetMapping(value = "/aUser")
    public User aUser(){
        User user = new User();
        user.setUsername("zhouning");
        user.setAge(22);
        return user;
    }
}
