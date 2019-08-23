package org.springframework.studymvc.web;

import org.springframework.studymvc.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周宁
 * @Date 2019-08-19 9:37
 */
@RestController
@RequestMapping("/user")
public class UserController2 {

    @ModelAttribute("user")
    public User User(){
        User user = new User();
        user.setUsername("daning");
        return user;
    }

    @GetMapping(value = "/aUser")
    public User aUser(@ModelAttribute("user") User user){

        user.setAge(22);
        System.out.println(user.getUsername());
        return user;
    }
}
