package org.springframework.study.day17.pojo;

import com.gysoft.jdbc.annotation.Table;

/**
 * @author 周宁
 * @Date 2019-07-29 14:51
 */
@Table(name = "tb_test3")
public class TbTest3 {
    private Integer id;

    private String context;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
