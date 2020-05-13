package com.example.pc.newble.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Vector;

import static android.content.ContentValues.TAG;


public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "productssss.db";
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRODUCTNAME = "productname";

    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DATA = "data";   // typo，应该是date
    public static final String COLUMN_VOLTAGE = "voltage";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_CHANNEL = "channel";
    public static final String COLUMN_ADDRESS = "address";

    private final int validThreshold = 150;

    public SQLiteDatabase db = getWritableDatabase();

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_PRODUCTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCTNAME + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_DATA + " TEXT, " +
                COLUMN_VOLTAGE + " TEXT, " +
                COLUMN_LONGITUDE + " TEXT, " +
                COLUMN_LATITUDE + " TEXT, " +
                COLUMN_CHANNEL + " TEXT, " +
                COLUMN_ADDRESS + " TEXT " +
                ");";
        db.execSQL(query);
        // query = "INSERT INTO " + TABLE_PRODUCTS + "(productname) VALUES(\"Apple\");";
        // db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_PRODUCTS;
        db.execSQL(query);
        onCreate(db);
    }

    // Add a new item to the database
    public void addItem(Products product) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCTNAME, product.get_productName());
        values.put(COLUMN_TIME, product.time);
        values.put(COLUMN_DATA, product.data);
        values.put(COLUMN_VOLTAGE, product.voltage);
        values.put(COLUMN_LONGITUDE, product.longitude);
        values.put(COLUMN_LATITUDE, product.latitude);
        values.put(COLUMN_CHANNEL, product.channel);
        values.put(COLUMN_ADDRESS, product.address);

   //     SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }

    // 删除单个条目。
    public void removeItem(String productName) {
   //     SQLiteDatabase db  = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTNAME + "=\"" + productName + "\";");
    }

    // 删除所有条目。
    public void removeAllItems(){
   //     SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS + " WHERE 1");
    }

    /**
     * 查询某天的某个时间点的条目。
     * */
    public String getDataOfOneCertainTime(String date, int time){  //这里的time指的是一天中的86400秒处以60后转换成的数据点，即0点0分为1，0点2分为2
    //    Log.e(TAG, "T1 " + Instant.now());

        String retval = "none";
    //    SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\"" + " AND " + COLUMN_PRODUCTNAME + "=\"" + Integer.toString(time) + "\";" ;
        Log.e(TAG, "GetDataOfOneCertainTime: SQL输出是  " + query );

        //Log.e(TAG, "T2 " + Instant.now());

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);

/*        if (c.moveToFirst()) {
            do {

                retval = c.getString(c.getColumnIndex(COLUMN_VOLTAGE));
                Log.e(TAG, "GetDataOfOneCertainTime: 发现了 " + retval );
                break;
            } while (c.moveToNext());
        }*/


        // count: 范围内有效数据点的个数
        // sum: 范围内有效数据点的数值之和
        int count = 0;
        double sum = 0;

        while (c.moveToNext()){

            // 消除大于 150 的点
            if (Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_VOLTAGE))) > 150 ){
                continue;
            }

            count += 1;
            sum += Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_VOLTAGE)));
        }
        if (count >= 1){
            retval = Double.toString(sum / count);
        }

       // Log.e(TAG, "T3 " + Instant.now());
        c.close();//关闭cursor 防止爆栈
     //   db.close();

        //Log.e(TAG, "T4 " + Instant.now());

        return retval;
    }

    /**
     * 计算某个特定小时内，超过某个特定阈值的百分比。
     * date: 日期
     * hour: 第几小时。例如 8：00～9：00 则为 8。
     * threshold: 阈值
     * */
    public int getCountBiggerThanACertainValue(String date, int hour, int threshold){
        // time:
        int time = hour * 60;
        int count = 0;
        int totalNumber = 0;

        for (int i=0; i<60; i++){
            // 对于每一分钟进行查询
            String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\"" + " AND " + COLUMN_PRODUCTNAME + "=\"" + Integer.toString(time + i) + "\";" ;
            Log.e(TAG, "GetDataOfOneCertainTime: SQL输出是  " + query );

            Cursor c = db.rawQuery(query, null);
            while (c.moveToNext()){
                // 如果大于阈值，count++
                if (Integer.parseInt(c.getString(c.getColumnIndex(COLUMN_VOLTAGE))) > threshold ){
                    count += 1;
                }
                totalNumber += 1;
            }
            c.close();
        }
        if (totalNumber == 0){
            return 0;
        } else {
            return (int) count * 100 / totalNumber;
        }

    }

    /**
     * 计算某个特定小时内，有效数据的标准差。
     * date: 日期
     * hour: 第几小时。例如 8：00～9：00 则为 8。
     * */
    public double getStdDivInACertainHour(String date, int hour){
        // time:
        int time = hour * 60;
        double sum = 0;
        int count = 0;
        DecimalFormat df = new DecimalFormat("0.00");
        Vector<Double> doubleVector = new Vector<>();

        for (int i=0; i<60; i++){
            String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\"" + " AND " + COLUMN_PRODUCTNAME + "=\"" + Integer.toString(time + i) + "\";" ;
            Log.e(TAG, "GetDataOfOneCertainTime: SQL输出是  " + query );

            Cursor c = db.rawQuery(query, null);
            while (c.moveToNext()){
                // 如果大于阈值，count++
                if (Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_VOLTAGE))) > this.validThreshold ){
                    continue;
                }

                double voltage = Double.parseDouble(c.getString(c.getColumnIndex(COLUMN_VOLTAGE)));
                sum += voltage;
                count += 1;
                doubleVector.add(voltage);
            }
            c.close();
        }
        if (count == 0){
            // 如果没有有效数据点，那么直接返回 0，避免除零错误。
            return 0;
        }
        double average = sum / count;

        // 计算标准差
        double dVar = 0;
        for (int i=0; i<doubleVector.size(); i++){
            dVar += (doubleVector.get(i) - average) * (doubleVector.get(i) - average);
        }
        double  result;
        result = Double.parseDouble(df.format(Math.sqrt(dVar / count)));
        return result;
    }



    /**
     * 查询某天的某个时间点的地址信息。
     * */
    public String getaddrOfOneCertainTime(String date, int time){
        String retval = "none";
     //   SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\"" + " AND " + COLUMN_PRODUCTNAME + "=\"" + Integer.toString(time) + "\";" ;
        Log.e(TAG, "GetDataOfOneCertainTime: SQL输出是  " + query );
        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                retval = c.getString(c.getColumnIndex(COLUMN_ADDRESS));
                Log.e(TAG, "GetDataOfOneCertainTime: 发现了 " + retval );
                break;
            } while (c.moveToNext());
        }
        c.close();//关闭cursor 防止爆栈
    //    db.close();
        return retval;
    }


    /**
     * 返回所欲查询的日期的所有记录。
     * */
    public Vector<String> getAllDataOfOneDayFromDatabase(String date) {
        Vector<String> availableDate = new Vector<>();

     //   SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\";" ;

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // 添加可用日期
                availableDate.add(c.getString(c.getColumnIndex(COLUMN_VOLTAGE)));
            } while (c.moveToNext());
        }
     //   db.close();
        return availableDate;
    }

    /**
     * 取出所有条目。纯粹的测试用。
     * */
    public String getAllItemsFromDatabase() {
        StringBuilder dbString = new StringBuilder();
    //    SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE 1";

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
      //          dbString.append(c.getString(c.getColumnIndex("productname")));
        //        dbString.append("   ");
                dbString.append(c.getString(c.getColumnIndex("time")));
          //      dbString.append("   ");
            //    dbString.append(c.getString(c.getColumnIndex("date")));

                dbString.append("\n");
            } while (c.moveToNext());
        }

     //   db.close();
        return dbString.toString();
    }

    // 析构函数
    protected void finalize(){
        db.close();
    }
}
