package org.springframework.study.day09;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author 周宁
 * @Date 2019-07-18 16:50
 */
public class TestListener implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(event instanceof TestEvent){
            ((TestEvent) event).print();
        }
    }
}
