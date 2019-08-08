import org.junit.Test;
import org.springframework.study.day14.jdkproxy.HelloInvocationHandler;
import org.springframework.study.day14.jdkproxy.HelloService;
import org.springframework.study.day14.jdkproxy.HelloServiceImpl;

/**
 * @author 周宁
 * @Date 2019-07-25 14:31
 */
public class Day14Test {

    @Test
    public void testJdkProxy(){
        HelloInvocationHandler helloInvocationHandler = new HelloInvocationHandler(new HelloServiceImpl());
        HelloService proxy = (HelloService) helloInvocationHandler.getProxy();
        proxy.gun();

    }
}
