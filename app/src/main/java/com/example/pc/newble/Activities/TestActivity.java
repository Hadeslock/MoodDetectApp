package com.example.pc.newble.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.pc.newble.R;
import com.example.pc.newble.SQLite.*;

import java.io.PrintWriter;
import java.io.StringWriter;

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
