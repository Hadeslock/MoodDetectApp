package com.example.pc.newble.TheUtils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
/**
 * Created by pc on 4/19/2019.
 *
 * 7/17/2019 修改：
 * 将存储 map 有关的函数放到了这里。
 */

public class DateUtil {
    @SuppressLint("SimpleDateFormat")
    public static String getNowDateTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("yyyyMMddhhmmss");
        return s_format.format(new Date());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getNowTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("HH:mm:ss");
        return s_format.format(new Date());
    }

    /**
     * 初始化一个map，将当天所有的电压值设为零。由于 java 不支持函数参数默认值，故请一定亲自传入。
     * 此处使用了 HashMap，不过由于项目较小，即使是红黑树 map，速度也不会有大的不同。
     * */
    public static HashMap<Integer, Double> InitMap(int TimeInterval) {
        HashMap<Integer, Double> hashmap = new HashMap();
        // DataCounts：每天需要多少个数据点
        int DataCounts = 86400 / TimeInterval;
        // 将一天内所有点初始化为 0。
        for (int i=0; i<DataCounts; i++) {
            hashmap.put(i, Double.parseDouble("0"));
        }
        return hashmap;
    }




}
