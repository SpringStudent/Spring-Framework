package org.springframework.study.day10;

import org.springframework.beans.factory.FactoryBean;

import java.text.SimpleDateFormat;

/**
 * @author 周宁
 * @Date 2019-07-19 16:49
 */
public class TestFactoryBean implements FactoryBean<HaveDateBean> {

    private String str;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public HaveDateBean getObject() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        HaveDateBean haveDateBean = new HaveDateBean();
        haveDateBean.setDate(sdf.parse(str));

        return haveDateBean;
    }

    @Override
    public Class<?> getObjectType() {
        return HaveDateBean.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
