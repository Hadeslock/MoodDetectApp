package com.example.pc.newble.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.newble.R;
import com.example.pc.newble.SQLite.*;
import com.example.pc.newble.TheUtils.FileUtils;

//import junit.framework.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

/**
 * 此 Activity 仅作为 Debug 用途。
 * */

public class TestActivity extends AppCompatActivity {

    private final String TAG = "TestActivity";

    private MyDBHandler dbHandler;
    private TextView textViewOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textViewOutput = findViewById(R.id.text_test_activity);

        try {
            dbHandler = new MyDBHandler(this, null, null, 1);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.i(TAG, errors.toString());
        }

   //


        listItems();

        Button button = findViewById(R.id.button_test_delete);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.removeAllItems();
                listItems();
            }
        });

        Button button1 = findViewById(R.id.button_get_data_from_csv);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "正在从csv文件恢复数据......", Toast.LENGTH_SHORT).show();
                // 读取所有文件名
                Vector<String> filesAllName = FileUtils.getFilesAllName(FileUtils.getSDCardPath() + "/bletest/");
                // 将每个文件读出，存入数据库
                for (int i=0; i<filesAllName.size(); i++) {
                    String file = filesAllName.get(i);

                    try {
                        BufferedReader in = new BufferedReader(new FileReader(file));
                        String str = new String();

                        while ((str = in.readLine()) != null) {
                            if (str.isEmpty() == true) {
                                // 本行是空行，直接跳过
                                continue;
                            } else {
                                try {
                                    // 读取这一行的数据
                                    String items[] = str.split(",");
                                    String date = items[1];
                                    String time = items[2];
                                    String voltage = (items[3]);
                                    String longitude = (items[4]);
                                    String latitude = (items[5]);
                                    String addressStr = items[6];
                                    String channel = items[7];
                                    Products product = new Products(date, time, voltage, longitude, latitude, addressStr, channel);

                                    dbHandler.addItem(product);
                                } catch (Exception e) {
                                    Log.e(TAG, "readFromCsvAndSaveToSQLite: Something wrong in retrieving data from the csv file. " +
                                            "It might be helpful to get the csv file checked.");
                                    e.printStackTrace();
                                }

                            }
                        }

                    } catch (java.io.IOException e) {
                        Log.e(TAG, "readVoltageFromFile: 读取出现了错误！！！");
                    }
                }
            }
        });


    }

    public void listItems() {
        try {
            Log.e(TAG, "listItems: 进入try过程……");
            String dbString = dbHandler.getAllItemsFromDatabase();
            textViewOutput.setText(dbString);

            Log.i(TAG, "Invoked: List Items Method. ");
        } catch (Exception e){
            Log.e(TAG, "listItems: 出错了！！出错了！！" );
            e.printStackTrace();
        }
    }
}
