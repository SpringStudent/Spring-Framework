import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.study.day07.MyXmlBeanFactory;
import org.springframework.study.day08.DatePropertyEditor;
import org.springframework.study.day08.ObscenityRemovingBeanFactoryPostProcessor;
import org.springframework.study.day08.ObsentityBean;
import org.springframework.study.day08.UserManager;

import java.util.Date;

/**
 * @author 周宁
 * @Date 2019-07-17 15:55
 */
public class Day08Test {

    @Test
    public void testPropertyEditor(){
        BeanFactory beanFactory =new MyXmlBeanFactory(new ClassPathResource("userManagerPropertySetter.xml"));
        ((MyXmlBeanFactory) beanFactory).registerCustomEditor(Date.class,DatePropertyEditor.class);
        UserManager userManager = (UserManager) beanFactory.getBean("userManager");
        System.out.println(userManager.toString());
    }

    @Test
    public void testBeanFactoryPostProcessor(){
        BeanFactory ac = new XmlBeanFactory(new ClassPathResource("beanFactoryPostProcessorTest.xml"));
        ObscenityRemovingBeanFactoryPostProcessor obscenityRemovingBeanFactoryPostProcessor = new ObscenityRemovingBeanFactoryPostProcessor();

        obscenityRemovingBeanFactoryPostProcessor.postProcessBeanFactory((ConfigurableListableBeanFactory) ac);

        ObsentityBean obsentityBean = ac.getBean(ObsentityBean.class);
        System.out.println(obsentityBean);
    }
}
