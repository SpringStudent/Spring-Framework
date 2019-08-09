package org.springframework.studymvc.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author 周宁
 * @Date 2019-08-09 10:07
 */
public class UserServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute("username","zhouning");
        servletContext.setAttribute("age",26);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("UserServletContextListener destroyed");
    }
}
