package com.example.pc.newble.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.content.Intent;
import android.widget.Toast;

import com.example.pc.newble.R;
import com.example.pc.newble.TheUtils.FileUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import com.example.pc.newble.SQLite.*;

public class ChooseHistActivity extends AppCompatActivity {

    // existingDataUI: 2008年1月1日9点48分21秒
    // existingData：20080101094821
    private Vector<String> existingDataUI;
    private Vector<String> existingData;

    private MyDBHandler dbHandler;

    private ListView listView;
    private static final String TAG = "ChooseHistActivity: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_hist);

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

        // 添加ListView项
        getAvailableHistData();
        // 哈希去重复。LinkedHashSet可以保持输出顺序与进入顺序一致
        Set<String> set = new LinkedHashSet<String>(existingData);
        Log.e(TAG, "onCreate: 列表：existingData" + set );
        existingData = new Vector<String>(set);
        Set<String> set2 = new LinkedHashSet<String>(existingDataUI);
        Log.e(TAG, "onCreate: 列表：existingDateUI" + set2);
        existingDataUI = new Vector<String>(set2);

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


   /**
    * 获取已储存的信息条目，并将它们添加到existingData中，以便用户点选
    * 返回值：Vector<String> 类型的原始数据。
    * */
    protected void getAvailableHistData(){

        // 获取已存档信息的检索
        String path = FileUtils.getSDCardPath() + "/bletest/DataList.txt";
        Vector<String> retval = FileUtils.readTextFromFile(path);
        // 将从数据库中读取的每一条信息添加到 existingData 里
        for (String item : retval){
            Log.e(TAG, "getAvailableHistData: 条目" + item );
            String string = item.substring(0,4) + "年" + item.substring(4,6) + "月" + item.substring(6,8) + "日";
                    //+ item.substring(8,10) + "时" + item.substring(10,12) + "分" + item.substring(12,14) + "秒";
            existingDataUI.add(string);
            existingData.add(item);
        }

    }

}

//Adapter.notifyDataSetChanged()