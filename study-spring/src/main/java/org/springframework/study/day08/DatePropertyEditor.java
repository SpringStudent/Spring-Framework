package org.springframework.study.day08;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 周宁
 * @Date 2019-07-17 15:49
 */
public class DatePropertyEditor extends PropertyEditorSupport {

    private String format = "yyyy-MM-dd";

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date d = sdf
                    .parse(text);
            this.setValue(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
