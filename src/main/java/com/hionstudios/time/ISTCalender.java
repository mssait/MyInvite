package com.hionstudios.time;

import java.util.GregorianCalendar;

public class ISTCalender extends GregorianCalendar {
    public ISTCalender() {
        super();
        setTimeZone(TimeUtil.TIMEZONE);
    }
}
