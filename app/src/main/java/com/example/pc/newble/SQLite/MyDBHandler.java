package com.example.pc.newble.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pc.newble.TheUtils.DateUtil;

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
                COLUMN_CHANNEL + " TEXT " +
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

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }

    // 删除单个条目。
    public void removeItem(String productName) {
        SQLiteDatabase db  = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTNAME + "=\"" + productName + "\";");
    }

    // 删除所有条目。
    public void removeAllItems(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PRODUCTS + " WHERE 1");
    }

    /**
     * 查询某天的某个时间点的条目。
     * */
    public String getDataOfOneCertainTime(String date, int time){
        String retval = "none";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\"" + " AND " + COLUMN_TIME + "=\"" + Integer.toString(time) + "\";" ;
        Log.e(TAG, "GetDataOfOneCertainTime: SQL输出是  " + query );
        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                retval = c.getString(c.getColumnIndex(COLUMN_VOLTAGE));
                Log.e(TAG, "GetDataOfOneCertainTime: 发现了 " + retval );
                break;
            } while (c.moveToNext());
        }
        db.close();
        return retval;
    }

    /**
     * 返回所有有记录的日期。用于构建 ChooseHistActivity 的 ListView。
     * */
    public Vector<String> getAllAvailableDateFromDatabase() {
        Vector<String> availableDate = new Vector<>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTNAME + "=\"" + "available" + "\";" ;

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // 添加可用日期
                availableDate.add(c.getString(c.getColumnIndex(COLUMN_DATA)));
            } while (c.moveToNext());
        }
        db.close();
        return availableDate;
    }

    /**
     * 返回所欲查询的日期的所有记录。
     * */
    public Vector<String> getAllDataOfOneDayFromDatabase(String date) {
        Vector<String> availableDate = new Vector<>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_DATA + "=\"" + date + "\";" ;

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                // 添加可用日期
                availableDate.add(c.getString(c.getColumnIndex(COLUMN_VOLTAGE)));
            } while (c.moveToNext());
        }
        db.close();
        return availableDate;
    }

    /**
     * 取出所有条目。在当前版本下，除了测试用途，最好避免使用这个函数。
     * */
    public String getAllItemsFromDatabase() {
        StringBuilder dbString = new StringBuilder();
        SQLiteDatabase db = getWritableDatabase();
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
                Log.e(TAG, "getAllItemsFromDatabase: 哦乒乒乓乓乒乒乓乓" + dbString );
                dbString.append("\n");
            } while (c.moveToNext());
        }

        db.close();
        return dbString.toString();
    }
}
