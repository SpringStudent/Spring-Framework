package org.springframework.study.day17.service;

import com.gysoft.jdbc.bean.SQL;
import org.springframework.study.day17.dao.TbTest2Dao;
import org.springframework.study.day17.dao.TbTest3Dao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 周宁
 * @Date 2019-08-08 15:24
 */
public class TestPropagationServiceImpl implements TestPropagationService{

    private TbTest2Dao tbTest2Dao;
    private TbTest3Dao tbTest3Dao;

    public void setTbTest2Dao(TbTest2Dao tbTest2Dao) {
        this.tbTest2Dao = tbTest2Dao;
    }

    public void setTbTest3Dao(TbTest3Dao tbTest3Dao) {
        this.tbTest3Dao = tbTest3Dao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void test() throws Exception {
        test2();
        test3();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void test2() throws Exception {
        tbTest2Dao.insertWithSql(new SQL().insert("id","context").values(0,"1"));

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void test3() throws Exception {
        tbTest3Dao.insertWithSql(new SQL().insert("id","context").values(0,"2"));
        System.out.println(1/0);
    }


    private TbTest2Service tbTest2Service;
    private TbTest3Service tbTest3Service;

    public void setTbTest2Service(TbTest2Service tbTest2Service) {
        this.tbTest2Service = tbTest2Service;
    }

    public void setTbTest3Service(TbTest3Service tbTest3Service) {
        this.tbTest3Service = tbTest3Service;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void testService() throws Exception {
        tbTest2Service.test2();

        tbTest3Service.test3();


    }
}
