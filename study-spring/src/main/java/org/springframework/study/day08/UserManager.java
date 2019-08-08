package org.springframework.study.day08;

import java.util.Date;

/**
 * @author 周宁
 * @Date 2019-07-17 15:40
 */
public class UserManager {

    private Date dataValue;

    public Date getDataValue() {
        return dataValue;
    }

    public void setDataValue(Date dataValue) {
        this.dataValue = dataValue;
    }

    @Override
    public String toString() {
        return "UserManager{" +
                "dataValue=" + dataValue +
                '}';
    }
}
