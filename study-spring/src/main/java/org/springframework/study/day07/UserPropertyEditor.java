package org.springframework.study.day07;

import java.beans.PropertyEditorSupport;

/**
 * @author 周宁
 * @Date 2019-07-16 9:47
 */
public class UserPropertyEditor extends PropertyEditorSupport {
    @Override
    public String getAsText() {
        User user = (User) getValue();
        return user.toString();
    }
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] fields = text.split(",");
        if (fields.length != 3) {
            throw new IllegalArgumentException("User 属性配置错误");
        }
        User user = new User();
        try {
            int id = Integer.parseInt(fields[0]);
            user.setId(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("User的属性配置错误");
        }
        user.setName(fields[1]);
        user.setAddress(fields[2]);
        setValue(user);
    }
}
