package org.springframework.study.day17.dao;

import com.gysoft.jdbc.dao.EntityDaoImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.study.day17.pojo.TbTest3;

/**
 * @author 周宁
 * @Date 2019-07-29 14:51
 */
public class TbTest3DaoImpl extends EntityDaoImpl<TbTest3,Integer> implements TbTest3Dao {

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
}
