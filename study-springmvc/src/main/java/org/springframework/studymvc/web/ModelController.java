package org.springframework.studymvc.web;

import org.springframework.stereotype.Controller;
import org.springframework.studymvc.model.User;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 周宁
 * @Date 2019-08-23 10:13
 */
@Controller
@RequestMapping("/model")
@SessionAttributes("user")
public class ModelController {

    @RequestMapping("/as/param")
    public String asParam(@ModelAttribute("user") User user) {
        System.out.println(user);
        return "userinfo";
    }

    @ModelAttribute
    public User initUser() {
        User user = new User();
        user.setUsername("laoning");
        return user;
    }

    @RequestMapping("/as/method")
    public String asMethod(@ModelAttribute("user") User user) {
        return "userinfo";
    }


}
