import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day16.service.TbTestService;
import org.springframework.study.day17.service.TestPropagationService;

/**
 * @author 周宁
 * @Date 2019-07-29 14:50
 */
public class Day17Test {

    @Test
    public void testAnnotationComplexTransaction() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("annotationComplexTranscation.xml");
        TestPropagationService testPropagationService = ac.getBean(TestPropagationService.class);
        testPropagationService.testService();
    }

}
