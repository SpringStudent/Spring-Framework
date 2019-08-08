package org.springframework.study.day01;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;

/**
 * @author 周宁
 * @Date 2019-07-04 17:28
 */
public class MyXmlBeanFactory extends DefaultListableBeanFactory {

    private final XmlBeanDefinitionReader reader;

    {
        reader = new XmlBeanDefinitionReader(this);
        reader.setDocumentReaderClass(DefaultBeanDefinitionDocumentReaderExtend.class);
    }

    public MyXmlBeanFactory(Resource resource) {
        super(null);
        reader.loadBeanDefinitions(resource);
    }
}
