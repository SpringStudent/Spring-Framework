import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day06.AnComplexBean;

/**
 * @author 周宁
 * @Date 2019-07-13 12:08
 */
public class Day06Test {

    @Test
    public void testAnComplexBean(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("anComplexBeanTest.xml");
        AnComplexBean anComplexBean = (AnComplexBean) ac.getBean("anComplexBean");
        System.out.println(anComplexBean.getAttr1());
    }
}
