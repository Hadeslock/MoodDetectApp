package com.example.pc.newble.SQLite;


import android.util.Log;

import com.example.pc.newble.TheUtils.DateUtil;

import static com.example.pc.newble.Activities.RetrieveData.TAG;


public class Products {
    private int _id;
    private String _productName;

    // 懒得写get 和 set 了……
    public String data;//其实是date
    public String time;
    public String voltage;
    public String longitude;
    public String latitude;
    public String channel;
    public String address;

    public void DefaultInitialization(){
        this._productName = "不存在的记录";
        this.data = "2019/1/1";
        this.time = "00:01";
        this.voltage = "not_available";
        this.longitude = "not_available";
        this.latitude = "not_available";
        this.channel = " 第5号 ";
        this.address = "not_available";
    }

    /**
     * 默认构造函数。
     * */
    public Products() {
        this.DefaultInitialization();
    }

    /**
     * 由蓝牙直接添加 entry 到数据库的构造函数。
     * */
    public Products(Float voltage) {
        this.DefaultInitialization();
        this._productName = "存在的记录";
        this.voltage = voltage.toString();
        this.time  = Integer.toString(TransTimeToInteger(DateUtil.getNowTime()));
        this.data = DateUtil.getNowDateTime().substring(0, 8);
    }

    /**
     * 由蓝牙直接添加 entry 到数据库的构造函数。包含经纬度。
     * */
    public Products(Float voltage, Double longitude, Double latitude,String address) {
        this.DefaultInitialization();
        this._productName = "存在的记录";
        this.voltage = voltage.toString();
        try{
            this.latitude = latitude.toString();
            this.longitude = longitude.toString();
            this.latitude = address;
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "Products: 定位数据读取失败，已在数据库内写入0" );
        }

        this.time  = Integer.toString(TransTimeToInteger(DateUtil.getNowTime()));
        this.data = DateUtil.getNowDateTime().substring(0, 8);
    }

    /**
     * 用于存储有记录的日期的构造函数。
     * */
    public Products(String _productName) {
        this.DefaultInitialization();
        this._productName = "available";
        this.data = DateUtil.getNowDateTime().substring(0, 8);
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_productName() {
        return _productName;
    }

    public void set_productName(String _productName) {
        this._productName = _productName;
    }

    /**
     * 将一个"HH:mm:ss"格式的String化为一个int，代表当天的第几个时间点
     * TODO 若更改此处的 TIME_INTERVAL，RetrieveData.java的time interval一块改了。
     * */
    public static int TransTimeToInteger(String time) {
        int timeInterval = 60;
        int hour = Integer.parseInt(time.substring(0,2));
        int minute = Integer.parseInt(time.substring(3, 5));
        int second = Integer.parseInt(time.substring(6, 8));
        int retval = 3600 / timeInterval * hour +
                60 / timeInterval * minute +
                second / timeInterval;
        return retval;
    }
}
