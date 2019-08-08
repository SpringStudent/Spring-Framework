package org.springframework.study.day03;

import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author 周宁
 * @Date 2019-07-08 15:49
 */
public class UserBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return User.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String userName = element.getAttribute("userName");
        String email = element.getAttribute("email");
        if(StringUtils.hasText(userName)){
            builder.addPropertyValue("userName",userName);
        }

        if(StringUtils.hasText(email)){
            builder.addPropertyValue("email",email);
        }
    }

    @Override
    protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {

    }
}
