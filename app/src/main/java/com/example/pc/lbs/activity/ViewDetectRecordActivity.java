package com.example.pc.lbs.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pc.lbs.R;
import com.example.pc.lbs.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class ViewDetectRecordActivity extends AppCompatActivity {

    private final ArrayList<String> fileNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_detect_record);

        //获取组件
        ListView detectRecordLV = findViewById(R.id.lv_detect_record);

        //获取输出目录下所有文件
        File filePath = new File(FileUtils.baseDirPath);
        File[] files = filePath.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            fileNames.add(fileName);
        }

        //设置数据适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
        detectRecordLV.setAdapter(adapter);

        //设置点击事件
        detectRecordLV.setOnItemClickListener((parent, view, position, id) -> {
            DetectRecordAnalyseActivity.actionStart(ViewDetectRecordActivity.this,
                    FileUtils.baseDirPath + fileNames.get(position) + ".csv");
        });
    }
}