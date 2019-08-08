import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.study.day02.constructor.Person;
import org.springframework.study.day02.lookup.GetBeanTest;
import org.springframework.study.day02.replace.TestChangeMethod;
import org.springframework.study.day02.value.MyValue;

/**
 * @author 周宁
 * @Date 2019-07-05 16:33
 */
public class Day02Test {

    @Test
    public void testlooKUp(){
        BeanFactory beanFactory =new XmlBeanFactory(new ClassPathResource("lookUpTest.xml"));
        GetBeanTest getBeanTest = (GetBeanTest) beanFactory.getBean("getBeanTest");
        getBeanTest.showMe();
    }

    @Test
    public void testReplace(){
        BeanFactory beanFactory =new XmlBeanFactory(new ClassPathResource("replaceTest.xml"));
        TestChangeMethod testChangeMethod = (TestChangeMethod) beanFactory.getBean("testChangeMethod");
        testChangeMethod.changeMe();
    }

    @Test
    public void testConstructor(){
        BeanFactory beanFactory =new XmlBeanFactory(new ClassPathResource("consturctorTest.xml"));
        Person person = (Person) beanFactory.getBean("person");
        System.out.println(person.getList());
    }

    @Test
    public void testValue(){
        BeanFactory beanFactory =new XmlBeanFactory(new ClassPathResource("valueTest.xml"));
        MyValue myValue = (MyValue) beanFactory.getBean("myValue");
        System.out.println(myValue.getOh());
    }
}
