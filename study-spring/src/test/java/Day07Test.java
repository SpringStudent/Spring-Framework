import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.study.day07.*;

import java.util.Map;
import java.util.Properties;

/**
 * @author 周宁
 * @Date 2019-07-15 19:24
 */
public class Day07Test {

    @Test
    public void testMyEventListener(){
        BeanFactory beanFactory =new MyXmlBeanFactory(new ClassPathResource("eventListenerTest.xml"));
    }

    @Test
    public void testInstantiationAwareBeanPostProcessor(){
        BeanFactory beanFactory =new MyXmlBeanFactory(new ClassPathResource("instantiationAwareBeanPostPorcessorTest.xml"));
        ((MyXmlBeanFactory) beanFactory).addBeanPostProcessor(new MyInstantiationAwareBeanPostProcessor());

        ProBean proxyBean = (ProBean) beanFactory.getBean("proBean");
        proxyBean.doSomething();
    }

    @Test
    public void testPropertyEditor(){
        BeanFactory beanFactory =new MyXmlBeanFactory(new ClassPathResource("propertyEditorTest.xml"));
        ((MyXmlBeanFactory) beanFactory).registerCustomEditor(User.class,UserPropertyEditor.class);
        UserHolder userHolder = (UserHolder) beanFactory.getBean("userHolder");
        System.out.println(userHolder.getUser().toString());
    }

    @Test
    public void testSpringPropertyEditor(){
        PropertiesEditor propertiesEditor = new PropertiesEditor();
        propertiesEditor.setAsText("username=zhouning\r\npassword=11111111111");
        Properties p = (Properties) propertiesEditor.getValue();
        for(Map.Entry<Object,Object> e : p.entrySet()){
            System.out.println("key:"+e.getKey());
            System.out.println("value:"+e.getValue());
        }
    }
}
