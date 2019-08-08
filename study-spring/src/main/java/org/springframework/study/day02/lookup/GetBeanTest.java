package org.springframework.study.day02.lookup;

/**
 * @author 周宁
 * @Date 2019-07-05 16:30
 */
public abstract class GetBeanTest {

    public void showMe(){
        this.getBean().showMe();
    }

    public abstract User getBean();
}
