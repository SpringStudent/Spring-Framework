package org.springframework.study.day12;

/**
 * @author 周宁
 * @Date 2019-07-22 16:32
 */
public class TargetImpl implements ITarget {
    @Override
    public void normal(String hello) {
        System.out.println("normal invoke{" + hello + "}");
    }

    @Override
    public void exception(String hello) {
        System.out.println("exception invoke{" + hello + "}");
        throw new RuntimeException("throw ex");
    }
}
