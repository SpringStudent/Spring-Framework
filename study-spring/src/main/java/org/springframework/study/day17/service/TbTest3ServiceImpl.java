package org.springframework.study.day17.service;

import com.gysoft.jdbc.bean.SQL;
import org.springframework.study.day17.dao.TbTest3Dao;
import org.springframework.study.day17.pojo.TbTest3;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 周宁
 * @Date 2019-08-08 15:46
 */
public class TbTest3ServiceImpl implements TbTest3Service {
    private TbTest3Dao tbTest3Dao;

    public void setTbTest3Dao(TbTest3Dao tbTest3Dao) {
        this.tbTest3Dao = tbTest3Dao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public void test3() throws Exception {
        tbTest3Dao.insertWithSql(new SQL().insert_into(TbTest3.class,"id", "context").values(0, "2"));
        if(1!=2){
            throw new Exception("asd");
        }
        System.out.println(123);
    }
}
