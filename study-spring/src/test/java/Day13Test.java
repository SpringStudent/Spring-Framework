import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day13.IDeclareParent;
import org.springframework.study.day13.NoMethodAspectBean;

/**
 * @author 周宁
 * @Date 2019-07-23 16:36
 */
public class Day13Test {

    @Test
    public void testAspectDeclare(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("aspectDeclareTest.xml");
        NoMethodAspectBean aspectBean = ac.getBean(NoMethodAspectBean.class);
        IDeclareParent iDeclareParent = (IDeclareParent) aspectBean;
        iDeclareParent.isANewMethod();
    }
    @Test
    public void testAspectDeclareXml(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("aspectDeclareXmlTest.xml");
        NoMethodAspectBean aspectBean = ac.getBean(NoMethodAspectBean.class);
        IDeclareParent iDeclareParent = (IDeclareParent) aspectBean;
        iDeclareParent.isANewMethod();
    }
}
