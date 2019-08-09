package org.springframework.studymvc.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 周宁
 * @Date 2019-08-09 9:44
 */
public class UserServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("UserServlet service");
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext servletContext = getServletContext();
        String username = servletContext.getAttribute("username").toString();
        String age = servletContext.getAttribute("age").toString();
        resp.setContentType("text/html");
        //返回数据或者视图
        PrintWriter out = resp.getWriter();
        out.println("<h1>" + username + age + "</h1>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        // 实际的逻辑是在这里
        PrintWriter out = resp.getWriter();
        out.println("<h1>" + "hello world" + "</h1>");
    }

    @Override
    public void destroy() {
        System.out.println("UserServlet destroy");
    }

    @Override
    public void init() throws ServletException {
        System.out.println("UserServlet init");
    }
}
