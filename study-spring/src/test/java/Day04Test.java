import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.study.day04.Car;

/**
 * @author 周宁
 * @Date 2019-07-08 20:01
 */
public class Day04Test {

    @Test
    public void testCarFactoryBean(){
        BeanFactory bf = new XmlBeanFactory(new ClassPathResource("carFactoryBeanTest.xml"));
        Car car = (Car) bf.getBean("car");
        System.out.println(car.getPrice());

    }
}
