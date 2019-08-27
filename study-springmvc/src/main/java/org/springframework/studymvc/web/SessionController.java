package org.springframework.studymvc.web;

import org.springframework.stereotype.Controller;
import org.springframework.studymvc.model.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 周宁
 * @Date 2019-08-23 10:29
 */
@Controller
public class SessionController {

    @RequestMapping("as/session")
    public String asSession(@SessionAttribute("user") User user, HttpServletRequest request){
        System.out.println(request.getSession().getAttribute("user"));
        System.out.println(user);
        return "userinfo";
    }
}
