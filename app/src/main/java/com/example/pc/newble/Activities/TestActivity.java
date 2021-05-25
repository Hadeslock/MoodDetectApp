package com.example.pc.newble.Activities;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

//import junit.framework.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


/**
 * 此 Activity 仅作为 Debug 用途。
 * */

public class TestActivity extends AppCompatActivity {

    private final String TAG = "TestActivity";

    private MyDBHandler dbHandler;
    private TextView textViewOutput;
    private LineChart mChart;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textViewOutput = findViewById(R.id.text_test_activity);
        Button button = findViewById(R.id.button_test_delete);
        Button button1 = findViewById(R.id.button_get_data_from_csv);
        mChart =  (LineChart) findViewById(R.id.chart);
        List<String> xval = new ArrayList<>();
        List<Double> yval =  new ArrayList<>();
        String path = FileUtils.getSDCardPath() + "/bletest/" + "20210524.csv";
        getdata(xval,yval,path);
        final List<String> finalXval = xval;
        final List<Double> finalYval = yval;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewOutput.append(finalXval.get(0)+" "+ finalXval.get(1)
                +" \n" + finalYval.get(0) +" " +finalYval.get(1));
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewOutput.append("hello ");
            }
        });
        // 设置描述
        mChart.setDescription("今日情绪指数回顾");
        //是否展示网格线
        mChart.setDrawGridBackground(false);
        //是否显示边界
        mChart.setDrawBorders(true);
        //是否可以拖动
        mChart.setDragEnabled(true);
        // 设置触摸模式
        mChart.setTouchEnabled(true);
        //设置XY轴动画效果
        mChart.animateY(600);
        mChart.animateX(1500);
        //
        // y坐标轴的设定。需要改y轴最大值的话可以在这里改
        YAxis yAxisLeft = mChart.getAxisLeft();
        yAxisLeft.setStartAtZero(true);
        yAxisLeft.setAxisMaxValue(100f);
        yAxisLeft.setTextSize(12f);
        // 右边的坐标轴。未来可以拓展为健康百分比之类的东西
        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setStartAtZero(true);
        yAxisRight.setAxisMaxValue(100f);
        yAxisRight.setEnabled(false);

        //
        //设置X轴位置
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        // 警戒线
        LimitLine ll = new LimitLine(70f, "警戒线");//修改警戒线为70
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        yAxisLeft.addLimitLine(ll);

    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getdata(List<String> xval,List<Double> yval,String path){
        String line;

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            while((line = br.readLine())!=null){
                List<String> column = Arrays.asList(line.split(","));
                xval.add(column.get(2));
                yval.add(Double.parseDouble(column.get(3)));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
