import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day10.HaveDateBean;
import org.springframework.study.day10.TestFactoryBean;

/**
 * @author 周宁
 * @Date 2019-07-19 10:53
 */
public class Day10Test {

    @Test
    public void testConversionService(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("conversionServiceTest.xml");
        HaveDateBean haveDateBean = ac.getBean(HaveDateBean.class);
        System.out.println(haveDateBean.getDate());
    }

    @Test
    public void testFactoryBean(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("factoryBeanInstanitateTest.xml");

        TestFactoryBean testFactoryBean = (TestFactoryBean) ac.getBean("testFactoryBean");


        System.out.println(testFactoryBean);
    }
}
