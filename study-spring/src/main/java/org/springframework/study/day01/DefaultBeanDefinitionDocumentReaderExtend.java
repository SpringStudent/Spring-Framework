package org.springframework.study.day01;

import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.w3c.dom.Element;

/**
 * @author 周宁
 * @Date 2019-07-04 17:25
 */
public class DefaultBeanDefinitionDocumentReaderExtend extends DefaultBeanDefinitionDocumentReader {
    @Override
    protected void preProcessXml(Element root) {
        System.out.println("我开始解析xml了");
    }

    @Override
    protected void postProcessXml(Element root) {
        System.out.println("我结束解析xml");
    }
}
