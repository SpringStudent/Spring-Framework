import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day03.User;

/**
 * @author 周宁
 * @Date 2019-07-08 16:02
 */
public class Day03Test {
    @Test
    public void testUserXsd(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("userXsd.xml");
        User user = (User) ac.getBean("user");
        System.out.println(user.getUserName());
    }
}
