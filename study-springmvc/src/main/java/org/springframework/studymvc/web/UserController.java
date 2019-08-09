package org.springframework.studymvc.web;

import org.springframework.studymvc.model.User;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 周宁
 * @Date 2019-08-09 9:11
 */
public class UserController  extends AbstractController {
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<User> users = new ArrayList<User>();
        User u1 = new User();
        u1.setAge(25);
        u1.setUsername("张帅");
        User u2 = new User();
        u2.setAge(26);
        u2.setUsername("周宁");
        users.add(u1);
        users.add(u2);
        return new ModelAndView("userlist","users",users);
    }
}
