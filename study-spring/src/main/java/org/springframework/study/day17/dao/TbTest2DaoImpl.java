package org.springframework.study.day17.dao;

import com.gysoft.jdbc.dao.EntityDaoImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.study.day16.pojo.TbTest;
import org.springframework.study.day17.pojo.TbTest2;

/**
 * @author 周宁
 * @Date 2019-07-29 14:51
 */
public class TbTest2DaoImpl extends EntityDaoImpl<TbTest2,Integer> implements TbTest2Dao {

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
}
