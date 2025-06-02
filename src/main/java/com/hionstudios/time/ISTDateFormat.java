package com.hionstudios.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ISTDateFormat extends SimpleDateFormat {

    public ISTDateFormat(String pattern) {
        super(pattern);
        setTimeZone(TimeUtil.TIMEZONE);
    }

    /**
     * Overriding only to handle the ParseException
     */
    @Override
    public Date parse(String source) {
        try {
            return super.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
