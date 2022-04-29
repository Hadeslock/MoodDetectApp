package com.example.pc.lbs.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import com.example.pc.lbs.R;


/**
 * Author: Hadeslock
 * Created on 2022/1/22 17:07
 * Email: hadeslock@126.com
 * Desc: 查看数据界面之后，分析结果是否正常
 */
public class ResultActivity extends AppCompatActivity {
    private TextView textViewOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        double mean = intent.getDoubleExtra("mean", 0);
        int peakNumbers = intent.getIntExtra("peakNumbers", 0);
        Button button = findViewById(R.id.button_plot);
        Colorstr colorstr = new Colorstr();

        getResult(colorstr, mean, peakNumbers);

        button.setBackgroundColor(colorstr.color);
        textViewOutput = findViewById(R.id.text_result);
        textViewOutput.setMovementMethod(ScrollingMovementMethod.getInstance());

        textViewOutput.setText(colorstr.str);
    }

    private void getResult(Colorstr colorstr, double mean, int peakNumbers) {

        if (mean > 70 || peakNumbers > 30) {
            colorstr.color = 0xFFFF0000;
            colorstr.str = "可能性较大";
        } else if (peakNumbers > 22) {
            colorstr.color = 0xF8F8FF00;
            colorstr.str = "可能性较小";
        } else {
            colorstr.color = 0xFF00FF00;
            colorstr.str = "正常";

        }

    }

    class Colorstr {
        int color;
        String str;
    }

}
