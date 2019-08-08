import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.study.day15.pojo.TbAccount;
import org.springframework.study.day15.service.TbAccountService;
import org.springframework.study.day15.service.TbUserService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周宁
 * @Date 2019-07-27 11:17
 */
public class Day15Test {

    @Test
    public void testRowMapper(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest.xml");
        TbUserService tbUserService = ac.getBean(TbUserService.class);
       System.out.println( tbUserService.getUsers());
    }

    @Test
    public void testDelete(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest.xml");
        TbUserService tbUserService = ac.getBean(TbUserService.class);
        tbUserService.delete();
    }


    @Test
    public void testInsert() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest2.xml");
        TbAccountService tbAccountService = ac.getBean(TbAccountService.class);

        List<TbAccount> accountList = new ArrayList<>();
        TbAccount t1 = new TbAccount();
        t1.setId(1);
        t1.setRealName("周");
        t1.setUserName("zhou");
        TbAccount t2 = new TbAccount();
        t2.setUserName("li");
        t2.setId(2);
        t2.setRealName("李");
        accountList.add(t1);
        accountList.add(t2);
        tbAccountService.insert(accountList);

    }

    @Test
    public void testUpdate() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest2.xml");
        TbAccountService tbAccountService = ac.getBean(TbAccountService.class);
        TbAccount tbAccount = new TbAccount();
        tbAccount.setId(1);
        tbAccount.setRealName("周宁");
        tbAccount.setUserName("zhouning");
        tbAccountService.update(tbAccount);
    }

    @Test
    public void testQueryAll() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest2.xml");
        TbAccountService tbAccountService = ac.getBean(TbAccountService.class);
        System.out.println(tbAccountService.querAll());
    }

    @Test
    public void testCount() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest2.xml");
        TbAccountService tbAccountService = ac.getBean(TbAccountService.class);
        System.out.println(tbAccountService.count());
    }

    @Test
    public void save() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("jdbcTemplateTest2.xml");
        TbAccountService tbAccountService = ac.getBean(TbAccountService.class);
        tbAccountService.save(3,"test","测试");
    }

}
