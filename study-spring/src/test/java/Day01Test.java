import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.study.day01.TestBean;
import org.springframework.study.day01.MyXmlBeanFactory;

/**
 * @author 周宁
 * @Date 2019-07-03 15:26
 */
public class Day01Test {

    @Test
    public void testXmlBeanFactory(){
        BeanFactory beanFactory =new XmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
        TestBean myTestBean = (TestBean) beanFactory.getBean("testBean");
        System.out.println(myTestBean.getTestStr());
    }

    @Test
    public void testMyXmlBeanFactory(){
        BeanFactory beanFactory =new MyXmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
        TestBean myTestBean = (TestBean) beanFactory.getBean("testBean");
        System.out.println(myTestBean.getTestStr());
    }
}
