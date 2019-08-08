package org.springframework.study.day16.dao;

import com.gysoft.jdbc.dao.EntityDaoImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.study.day16.pojo.TbTest;

/**
 * @author 周宁
 * @Date 2019-07-29 14:51
 */
public class TbTestDaoImpl extends EntityDaoImpl<TbTest,Integer> implements TbTestDao {

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
}
