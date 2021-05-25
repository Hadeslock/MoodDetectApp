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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

//import junit.framework.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 此 Activity 作为绘图显示数据功能
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
        String path = FileUtils.getSDCardPath() + "/bletest/" + "20210522.csv";
        String startTime = "14:55:13";
        String endTime = "15:11:13";
        getdata(xval,yval,path,startTime,endTime);
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
        initChart(mChart);
        LineData lineData = getLineData(xval,yval);

        mChart.clear();

        Log.e(TAG, "onClick: 清理完毕");
        showChart(lineData);
        Log.e(TAG,xval.size()+"");

    }
    private void initChart(LineChart mChart){
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
        //yAxisLeft.setAxisMaxValue(100f);
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
//        LimitLine ll = new LimitLine(70f, "警戒线");//修改警戒线为70
//        ll.setLineColor(Color.RED);
//        ll.setLineWidth(2f);
//        ll.setTextColor(Color.BLACK);
//        ll.setTextSize(12f);
//        yAxisLeft.addLimitLine(ll);
    }
    private void showChart(LineData lineData) {
        // 设置图表数据
        mChart.setData(lineData);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getdata(List<String> xval,List<Double> yval,String path,String startTime,String endTime){//读取CSV文件数据
        String line;

        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            while((line = br.readLine())!=null){
                List<String> column = Arrays.asList(line.split(","));
                if(timeCompare(column.get(2),startTime)&&
                        timeCompare(endTime,column.get(2))){
                    xval.add(column.get(2));
                    yval.add(Double.parseDouble(column.get(3)));
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public boolean timeCompare(String str1,String str2){ //比较两个时间的大小
        Boolean res = false;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        try{
            Date date1 = df.parse(str1);
            Date date2 = df.parse(str2);
            res = date1.getTime()>= date2.getTime();
        }catch (ParseException e){
            e.printStackTrace();
        }
        return res;
    }

    public LineData getLineData(List<String> xVals,List<Double> yvals){ //制作LineData
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < yvals.size(); i++) {
            float val = yvals.get(i).floatValue();//获取数据
            yVals.add(new Entry(val, i));
        }

        // 创建数据集
        LineDataSet set = new LineDataSet(yVals, "情绪指数");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.YELLOW);
        set.setLineWidth(0f);
        set.setCircleSize(0f);
        set.setFillAlpha(45);


        //设置曲线值的圆点是实心还是空心
        set.setDrawCircleHole(false);
        set.setValueTextSize(10f);
        //设置折线图填充
        set.setDrawFilled(true);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);


        // 创建数据集列表
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set);

        // 创建折线数据对象（第二个参数可以是set）
        LineData lineData = new LineData(xVals, dataSets);
        lineData.setDrawValues(false);
        lineData.setValueTextColor(Color.BLACK);
        lineData.setValueTextSize(9f);

        return lineData;
    }
}
