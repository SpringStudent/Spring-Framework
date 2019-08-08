package org.springframework.study.day03;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author 周宁
 * @Date 2019-07-08 15:55
 */
public class MyNameSpaceHandler  extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("user",new UserBeanDefinitionParser());
    }
}
