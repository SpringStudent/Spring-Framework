package org.springframework.study.day06;

import java.util.List;
import java.util.Map;

/**
 * @author 周宁
 * @Date 2019-07-13 11:57
 */
public class AnComplexBean {

    private int attr1;

    private Map<String,AttrBean> attr2;

    private List<AttrBean> attr3;

    private AttrBean attr4;

    private AttrInfo attrInfo;

    public void setAttr1(int attr1) {
        this.attr1 = attr1;
    }

    public AnComplexBean(Map<String, AttrBean> attr2, List<AttrBean> attr3) {
        this.attr2 = attr2;
        this.attr3 = attr3;
    }

    public int getAttr1() {
        return attr1;
    }

    public Map<String, AttrBean> getAttr2() {
        return attr2;
    }

    public List<AttrBean> getAttr3() {
        return attr3;
    }

    public AttrBean getAttr4() {
        return attr4;
    }

    public void setAttr2(Map<String, AttrBean> attr2) {
        this.attr2 = attr2;
    }

    public void setAttr3(List<AttrBean> attr3) {
        this.attr3 = attr3;
    }

    public void setAttr4(AttrBean attr4) {
        this.attr4 = attr4;
    }

    public AttrInfo getAttrInfo() {
        return attrInfo;
    }

    public void setAttrInfo(AttrInfo attrInfo) {
        this.attrInfo = attrInfo;
    }
}

