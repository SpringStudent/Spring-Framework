package org.springframework.studymvc.acini;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * @author 周宁
 * @Date 2019-08-13 16:46
 */
public class MyApplicationContextInitializer implements ApplicationContextInitializer<XmlWebApplicationContext> {
    @Override
    public void initialize(XmlWebApplicationContext applicationContext) {
        System.out.println("MyApplicationContextInitializer initialize");
    }
}
