package org.springframework.study.day02.lookup;

/**
 * @author 周宁
 * @Date 2019-07-05 16:29
 */
public class Teacher  extends User{

    @Override
    public void showMe() {
        System.out.println("i am teacher");
    }
}
