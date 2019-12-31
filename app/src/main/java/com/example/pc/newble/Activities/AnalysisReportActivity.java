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
       int count70[] = intent.getIntArrayExtra("count70") ; //获取超过70的数据点构成的一个数组，
       int count30[] = intent.getIntArrayExtra("count30") ;
       int countall[] = intent.getIntArrayExtra("countall") ;
       String address[] = intent.getStringArrayExtra("address") ;
        //Log.e("AnalysitActivity.this", "long:  " +recvDataLength);

        for(int i = 0;i < 24; i++){//输出数组的每个值到textview中
            String j;
            if (address[i]== "none") {
              //  j= count70[i]+" "+ count30[i] + " " + countall[i];
            }
            else{
              //  j= count70[i]+" "+ count30[i] + " " + countall[i] +  " " +address[i];
                if ((double)count30[i]/countall[i] > 2.0/3){
                    j = "在" + i + "时到" + (i+1)+ "时之间，您的情绪指数小于30的时间超过40分钟，该时间段您位于"+address[i]+"\n请继续保持";
                    addText(textView,j);
                }
                if ((double)count70[i]/countall[i] > 1.0/2){
                    j = "在" + i + "时到" + (i+1)+ "时之间，您的情绪指数大于70的时间超过30分钟，该时间段您位于"+address[i]+"\n这段时间是否遇到了什么令您不悦的事情，请放松您的心情";
                    addText(textView,j);
                }
            }

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
