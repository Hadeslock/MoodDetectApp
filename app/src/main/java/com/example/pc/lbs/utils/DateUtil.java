package com.example.pc.lbs.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pc on 4/19/2019.
 * <p>
 * 7/17/2019
 */

public class DateUtil {
    //获取当前日期
    public static String getNowDate() {
        return getNowDateTime().substring(0, 8);
    }

    //获取当前的日期和时间
    public static String getNowDateTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA);
        return s_format.format(new Date());
    }

    //获取当前的时间
    public static String getNowTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        return s_format.format(new Date());
    }


}
