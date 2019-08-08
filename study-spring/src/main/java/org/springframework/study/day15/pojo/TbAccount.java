package org.springframework.study.day15.pojo;

/**
 * @author 周宁
 * @Date 2019-07-29 9:42
 */
public class TbAccount {

    private Integer id;

    private String userName;

    private String realName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
