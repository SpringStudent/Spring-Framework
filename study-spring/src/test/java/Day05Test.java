import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day05.FactoryBean;

/**
 * @author 周宁
 * @Date 2019-07-10 15:12
 */
public class Day05Test {

    @Test
    public void testCreateBeanByFactory(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("createdBeanByFactoryTest.xml");
        FactoryBean staticFactoryBean = (FactoryBean) ac.getBean("factoryBean");
        System.out.println(staticFactoryBean.getName());
    }
}
