import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day11.AspectBean;

/**
 * @author 周宁
 * @Date 2019-07-20 14:01
 */
public class Day11Test {

    @Test
    public void testAspectJ() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("aspectJTest.xml");
        AspectBean aspectBean = ac.getBean(AspectBean.class);
        aspectBean.aspectTest();
    }

    @Test
    public void testXmlAop() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("aopXmlConfTest.xml");
        AspectBean aspectBean = ac.getBean(AspectBean.class);
        aspectBean.aspectTest();
    }

    @Test
    public void testXmlAop2() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("aopXmlConfTest2.xml");
        AspectBean aspectBean = ac.getBean(AspectBean.class);
        aspectBean.aspectTest();
    }
}
