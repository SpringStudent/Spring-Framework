package org.springframework.studymvc.event;

import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * @author 周宁
 * @Date 2019-08-20 19:18
 */
public class ServletRequestHandledEventListener implements ApplicationListener<ServletRequestHandledEvent> {
    @Override
    public void onApplicationEvent(ServletRequestHandledEvent event) {
        System.out.println(event.getRequestUrl());
    }
}
