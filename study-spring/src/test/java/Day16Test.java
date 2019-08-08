import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day16.service.TbTestService;

/**
 * @author 周宁
 * @Date 2019-07-29 14:50
 */
public class Day16Test {

    @Test
    public void testTxAdviceTransaction() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("txAdviceTranscation.xml");
        TbTestService tbTestService = ac.getBean(TbTestService.class);
        tbTestService.save("哈喽哈喽");
    }

    @Test
    public void testAnnotationTransaction() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("annotationTranscation.xml");
        TbTestService tbTestService = ac.getBean(TbTestService.class);
        tbTestService.insert("嗨喽嗨");
    }

    @Test
    public void testTransactionTemplate() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("transactionTemplateTest.xml");
        TbTestService tbTestService = ac.getBean(TbTestService.class);
        tbTestService.create("meow");
    }
}
