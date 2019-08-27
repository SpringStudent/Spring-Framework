package org.springframework.studymvc.web;

import org.springframework.studymvc.model.User;
import org.springframework.web.bind.annotation.*;

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
        user.setAge(22);
        user.setUsername("daning");
        return user;
    }
}
