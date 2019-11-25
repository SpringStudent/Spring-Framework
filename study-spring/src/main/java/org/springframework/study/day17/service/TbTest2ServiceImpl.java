package org.springframework.study.day17.service;

import com.gysoft.jdbc.bean.SQL;
import org.springframework.study.day17.dao.TbTest2Dao;
import org.springframework.study.day17.pojo.TbTest2;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 周宁
 * @Date 2019-08-08 15:45
 */
public class TbTest2ServiceImpl implements TbTest2Service{
    private TbTest2Dao tbTest2Dao;

    public void setTbTest2Dao(TbTest2Dao tbTest2Dao) {
        this.tbTest2Dao = tbTest2Dao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void test2() throws Exception {
        tbTest2Dao.insertWithSql(new SQL().insert_into(TbTest2.class,"id","context").values(0,"1"));
    }
}
