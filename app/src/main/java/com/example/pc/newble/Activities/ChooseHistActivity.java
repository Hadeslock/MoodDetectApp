package com.example.pc.newble.Activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.service.autofill.FillCallback;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ListView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pc.newble.Decorator.EventDecorator;
import com.example.pc.newble.Decorator.HighlightWeekendsDecorator;
import com.example.pc.newble.R;
import com.example.pc.newble.TheUtils.FileUtils;
import android.app.Activity;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import com.example.pc.newble.SQLite.*;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

public class ChooseHistActivity extends AppCompatActivity implements OnDateSelectedListener {

    // existingDataUI: 2008年1月1日9点48分21秒
    // existingData：20080101094821
      private Vector<String> existingDataUI;
      private Vector<String> existingData;

      private MyDBHandler dbHandler;
    //  private ListView listView;
    private static final String TAG = "ChooseHistActivity: ";
  //  private CalendarView calendarView;
    private MaterialCalendarView materialCalendarView;
    private TextView textView;

    /**
     * Time.MONTH及Calendar.MONTH 默认的月份为  0-11，
     * 所以使用的时候要自己加1.
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_hist);


        materialCalendarView = findViewById(R.id.calendarView);
     //   calendarView =  findViewById(R.id.cal);

        materialCalendarView.setOnDateChangedListener(this);

        textView = (TextView) findViewById(R.id.tv);

        Vector<CalendarDay> temp = this.checkHistData();//this

        materialCalendarView.addDecorators(
                new EventDecorator(Color.RED, temp),   //标红点
                new HighlightWeekendsDecorator()
        );
    }



    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {//日历日期选择
        //selected is no value on logcat
        Log.d("selected", "" + selected);
        //It can't be show
    //    Toast.makeText(this, "enterDateSelected" + date, Toast.LENGTH_SHORT).show();
        int flag=0;

        if (selected == true) {
            flag=1;
            //It can't be show
     //       Toast.makeText(this, "onClick" + date, Toast.LENGTH_SHORT).show();
            int year = date.getYear();
            int month = date.getMonth();
            int dayOfMonth = date.getDay();
            month++;
        //    Toast.makeText(ChooseHistActivity.this,
          //          "查询「" + year +  "年" + month + "月" + dayOfMonth + "日」的信息", Toast.LENGTH_LONG).show();
            if (flag==1){    //使用滚动进度条的方式阻止用户反复点击日期出现bug
            ProgressDialog progressDialog = new ProgressDialog(ChooseHistActivity.this);
            progressDialog.setTitle("正在打开");
            progressDialog.setMessage("请稍等");
            progressDialog.setCancelable(true);
            progressDialog.show();}
            // 补全文字
            String stringMonth = month < 10 ? "0" + Integer.toString(month) : Integer.toString(month);
            String stringDay = dayOfMonth < 10 ? "0" + Integer.toString(dayOfMonth) : Integer.toString(dayOfMonth);
            String string = Integer.toString(year) + stringMonth + stringDay;
            // 新的活动
            Intent intent = new Intent(ChooseHistActivity.this, RetrieveData.class);
            intent.putExtra("file_to_read", string);
            startActivity(intent);
            finish();  //跳转后直接销毁当前进程

        }
    }


    /**
     * 获取已储存的信息条目，并将它们添加到existingData中，以便用户点选
     * 返回值：Vector<String> 类型的原始数据。
     * */
    protected void getAvailableHistData(){//读取所有的csv文件

        // 获取已存档信息的检索
        Vector<String> strings = FileUtils.getFilesAllName(FileUtils.getSDCardPath() + "/bletest/");
        for (String item : strings){//从strings的第一个字符串开始遍历

                int i = item.length();
                Log.e(TAG, "getAvailableHistData: 哈哈哈哈" + item + "  " );
                if (item.substring(i-4, i).equals(".csv")){
                    Log.e(TAG, "getAvailableHistData: 这个是对的" );
                    // TODO 避免其他csv被涵盖进去
                    if (item.substring(i-12, i).equals("DataList.csv") == false){
                        existingData.add(item.substring(i-12, i));
                    }

                } else {
                    Log.e(TAG, "getAvailableHistData: 错了" );

                }


        }
        // 以下是从datalist里读取已有日期的代码
        String path = FileUtils.getSDCardPath() + "/bletest/DataList.txt";
        Vector<String> retval = FileUtils.readTextFromFile(path);
        // 将从数据库中读取的每一条信息添加到 existingData 里
        for (String item : retval){
            Log.e(TAG, "getAvailableHistData: 条目" + item );
            String string = item.substring(0, 4) + "年" + item.substring(4, 6) + "月" + item.substring(6, 8) + "日";
            //+ item.substring(8,10) + "时" + item.substring(10,12) + "分" + item.substring(12,14) + "秒";
    //        existingDataUI.add(string);
            existingData.add(item);
        }

    }

    private Vector<CalendarDay> checkHistData(){

        existingData = new Vector<String>();
        existingDataUI = new Vector<String>();

        // 实例化 dpHandler。
        try {
            dbHandler = new MyDBHandler(this, null, null, 1);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.i(TAG, errors.toString());
        }


        getAvailableHistData();//把datalist与csv的日期放到existingdata里面
        // 哈希去重复。LinkedHashSet可以保持输出顺序与进入顺序一致，set集合
        Set<String> set = new LinkedHashSet<String>(existingData);
        existingData = new Vector<String>(set);
        Set<String> set2 = new LinkedHashSet<String>(existingDataUI);
        existingDataUI = new Vector<String>(set2);

        // 将每个日期换成 CalendarDay
        Vector<CalendarDay> calendarDayVector= new Vector<>();
        for (int i=0; i<existingData.size(); i++){
            try {
                Log.e(TAG, "checkHistData: " + existingData.get(i));
                int year = Integer.parseInt(existingData.get(i).substring(0, 4));
                int month = Integer.parseInt(existingData.get(i).substring(4, 6));
                int day = Integer.parseInt(existingData.get(i).substring(6, 8));
                month--;
                CalendarDay calendarDay = new CalendarDay(year, month, day);
                calendarDayVector.add(calendarDay);
            } catch (Exception e){
                // 啥也不做
                Log.e(TAG, "checkHistData: csv有一个" );
            }

        }

        return calendarDayVector;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_hist);





        existingDataUI.add("🌏清空所有数据🌍");

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                ChooseHistActivity.this,   // Context上下文
                android.R.layout.simple_list_item_1,  // 子项布局id
                existingDataUI);                                // 数据
        ListView listView = (ListView) findViewById(R.id.hist_data);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            //parent 代表listView View 代表 被点击的列表项 position 代表第几个 id 代表列表编号
            public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                Log.e(TAG, "onItemClick: " + position + "  " + existingData.size() );
                if (position == existingData.size() ) {
                    android.widget.Toast.makeText(ChooseHistActivity.this, existingData.size() + "请在主界面跳转到csv测试中抹除数据库中的数据", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }

                android.widget.Toast.makeText(ChooseHistActivity.this, "打开文件" + existingData.get(position) + "\n耗时可能较长，请耐心等待", Toast.LENGTH_SHORT).show();
                String string = existingData.get(position);
                    Intent intent = new Intent(ChooseHistActivity.this, RetrieveData.class);
                    intent.putExtra("file_to_read", string);
                    startActivity(intent);


            }
        });
    }
    */


//Adapter.notifyDataSetChanged()

/*
  <CalendarView android:id="@+id/cal"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:firstDayOfWeek="3"
        android:shownWeekCount="7"
        android:focusedMonthDateColor="#FF8000"
        android:selectedWeekBackgroundColor="#9BFFFF"
        android:weekSeparatorLineColor="#0000FF"/>

        */