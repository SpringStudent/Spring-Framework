package org.springframework.study.day09;

import org.springframework.context.ApplicationEvent;

/**
 * @author 周宁
 * @Date 2019-07-18 16:47
 */
public class TestEvent extends ApplicationEvent {

    private String msg;

    public TestEvent(Object source) {
        super(source);
    }

    public TestEvent(Object source, String msg) {
        super(source);
        this.msg = msg;
    }

    public void print(){
        System.out.println(msg);
    }
}
