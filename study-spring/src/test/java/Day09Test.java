import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day09.TestEvent;

/**
 * @author 周宁
 * @Date 2019-07-18 16:52
 */
public class Day09Test {

    @Test
    public void testEvent(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("eventListenerTest.xml");
        TestEvent testEvent = new TestEvent("hello","msg");
        ac.publishEvent(testEvent);
    }

    @Test
    public void testBeanFactoryPostProcessor(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("testBeanFactoryPostProcessor.xml");

    }

    @Test
    public void testMergedBeanDefinitionPostProcessor(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("testMergedBeanDefinitionPostProcessor.xml");

    }
}
