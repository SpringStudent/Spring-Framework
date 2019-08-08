package org.springframework.study.day10;

import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 周宁
 * @Date 2019-07-19 10:47
 */
public class String2DateConverter implements Converter<String, Date> {
    @Override
    public Date convert(String source) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(source);
        } catch (ParseException e) {
            return null;
        }

        return date;
    }
}
