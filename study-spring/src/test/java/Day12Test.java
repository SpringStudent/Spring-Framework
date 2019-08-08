import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.study.day12.*;

/**
 * @author 周宁
 * @Date 2019-07-22 16:45
 */
public class Day12Test {

    @Test
    public void testBeforeAdvice(){
        TargetImpl target = new TargetImpl();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(new MethodBeforeAdviceImpl());
        ITarget proxy = (ITarget) proxyFactory.getProxy();
        proxy.normal("干");
    }

    @Test
    public void testReturnningAdvice(){
        TargetImpl target = new TargetImpl();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(new MethodAfterReturningImpl());
        ITarget proxy = (ITarget) proxyFactory.getProxy();
        proxy.normal("干");
    }

    @Test
    public void testThrowingAdvice(){
        TargetImpl target = new TargetImpl();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(new ThrowAdviceImpl());
        ITarget proxy = (ITarget) proxyFactory.getProxy();
        proxy.exception("干");
    }

    @Test
    public void testArroundAdvice(){
        TargetImpl target = new TargetImpl();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(new MethodInterceptorImpl());
        ITarget proxy = (ITarget) proxyFactory.getProxy();
        proxy.normal("干");
    }

    @Test
    public void testMultiAdvice(){
        TargetImpl target = new TargetImpl();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(new MethodBeforeAdviceImpl());
        proxyFactory.addAdvice(new MethodAfterReturningImpl());
        ITarget proxy = (ITarget) proxyFactory.getProxy();
        proxy.normal("干");
    }
}
