package com.example.pc.newble.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.pc.newble.R;

/**
*该活动用于获取RetrieveData这个活动的数据生成一份分析报告
*
*
*
*
 */
public class AnalysisReportActivity extends AppCompatActivity {
    private TextView textView;
    public static final String TAG = "AnalysisReportActivity.this";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysisreport);
        textView=findViewById(R.id.textView1);
        Intent intent = getIntent();//获取Intent对象
       int recvData[] = intent.getIntArrayExtra("DATA") ; //获取超过70的数据点构成的一个数组，及retrieveData中的count数组
        int recvDataLength = recvData.length ;
        //Log.e("AnalysitActivity.this", "long:  " +recvDataLength);

        for(int i = 0;i < 24; i++){//输出数组的每个值到textview中
            addText(textView,String.valueOf(recvData[i]));
          //  addText(textView,"1");
           // textView.append(String.valueOf(recvData[i]));
          //  Log.e("AnalysitActivity.this", "count:  " +recvData[i]);
        }
    }

    private void addText(TextView textView, String content) { 
        textView.append(content);
        textView.append("\n");
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        //  int offset = textView.getLineCount() * textView.getLineHeight();
        //  if (offset > textView.getHeight()) {
        //      textView.scrollTo(0, offset - textView.getHeight());
        //  }
    }
}
