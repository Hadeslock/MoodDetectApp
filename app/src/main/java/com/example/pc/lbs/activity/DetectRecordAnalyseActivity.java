package com.example.pc.lbs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pc.lbs.R;
import com.example.pc.lbs.utils.Complex;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 此 Activity 作为绘图显示数据并提供特征计算和分析功能
 */

public class DetectRecordAnalyseActivity extends AppCompatActivity {

    private String selectedFileName; //上一个活动传递过来的已选择的文件名

    // region 组件引用
    private TextView textViewOutput;
    private Button getResultBtn;
    private LineChart mChart;
    // endregion


    private final List<String> xval = new ArrayList<>();//x轴数据
    private final List<Double> yval = new ArrayList<>();//y轴数据
    private List<Double> yfft = new ArrayList<>();//fft后的数据
    private List<Double> yNormal;//归一化后的y轴数据
    private LineData lineData;//保存mchart的作图数据
    private ProgressDialog pd;//运算费时间时弹出的进度框
    private String str; //记录结果的字符串
    private int peakNumbers; //记录峰数作为评判依据之一
    private double mean; //记录平均值作为评判依据之一

    /*
     * 启动活动的方法
     * @author hadeslock
     * @date 2022/5/9 20:56
     * @param context 调用方法的地方的context
     * @param fileName 要查看的文件名，要求全路径
     * @return void
     */
    public static void actionStart(Context context, String fileName) {
        Intent intent = new Intent(context, DetectRecordAnalyseActivity.class);
        intent.putExtra("selectedFileName", fileName);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_record_analyse);

        //获取要显示的测试记录文件名
        Intent intent = getIntent();
        selectedFileName = intent.getStringExtra("selectedFileName");

        initView();
        initEvent();
    }

    //初始化组件引用
    private void initView() {
        textViewOutput = findViewById(R.id.text_test_activity);
        getResultBtn = findViewById(R.id.btn_getResult);
        mChart = findViewById(R.id.chart);
    }

    //初始化事件
    private void initEvent() {
        textViewOutput.setMovementMethod(ScrollingMovementMethod.getInstance());

        getResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(DetectRecordAnalyseActivity.this, "计算特征", "数据分析中，请稍后……");//开启弹窗
                new Thread() {//开启新线程做计算

                    @Override
                    public void run() {
                        //需要花时间计算的方法
                        double sum, std, var, median, rms, diff1Mean,
                                diff1Median, diff1Std, diff2Mean,
                                diff2Median, diff2Std, minRatio, maxRatio;

                        yNormal = normalLize(yval);
                        List<Integer> indMax = new ArrayList<>();
                        peakNumbers = findPeaks(yNormal, indMax, new ArrayList<Integer>(), 0.1);//计算峰数
                        List<Double> diff1;
                        List<Double> diff2;
                        sum = getSum(yval);
                        mean = getMean(yval, sum);
                        var = getVar(yval, mean);
                        std = getStd(yval, mean);
                        median = getMedian(yval);
                        rms = getRms(yval);
                        diff1 = getDiff(yval);
                        diff2 = getDiff(diff1);
                        diff1Mean = getMean(diff1, getSum(diff1));
                        diff1Median = getMedian(diff1);
                        diff1Std = getStd(diff1, diff1Mean);
                        diff2Mean = getMean(diff2, getSum(diff2));
                        diff2Median = getMedian(diff2);
                        diff2Std = getStd(diff2, diff2Mean);
                        double[] minmax = getMinMax(yval);
                        double min_ratio = minmax[0] / yval.size();
                        double max_ratio = minmax[1] / yval.size();

                        yfft = getYfft(yval);
                        double sumf = getSum(yfft);
                        double meanf = getMean(yfft, sum);
                        double medianf = getMedian(yfft);
                        double stdf = getStd(yfft, mean);
                        double varf = getVar(yfft, mean);
                        double rmsf = getRms(yfft);
                        List<Double> diff1f = getDiff(yfft);
                        List<Double> diff2f = getDiff(diff1);
                        double diff1Meanf = getMean(diff1, getSum(diff1));
                        double diff1Medianf = getMedian(diff1);
                        double diff1Stdf = getStd(diff1, diff1Mean);
                        double diff2Meanf = getMean(diff2, getSum(diff2));
                        double diff2Medianf = getMedian(diff2);
                        double diff2Stdf = getStd(diff2, diff2Mean);
                        double[] minmaxf = getMinMax(yfft);
                        double min_ratiof = minmaxf[0] / yfft.size();
                        double max_ratiof = minmaxf[1] / yfft.size();
                        str = "峰数: " + peakNumbers +
                                " 均值: " + String.format(Locale.CHINA, "%.2f", mean) +
                                " 方差: " + String.format(Locale.CHINA, "%.2f", var) +
                                " 标准差: " + String.format(Locale.CHINA, "%.2f", std) +
                                " 中值: " + String.format(Locale.CHINA, "%.2f", median) +
                                " 均方根:" + String.format(Locale.CHINA, "%.2f", rms)
                                + "\n一阶微分的均值: " + String.format(Locale.CHINA, "%.2f", diff1Mean) +
                                "一阶微分的均值 " + String.format(Locale.CHINA, "%.2f", diff1Median) +
                                "\n一阶微分的方差: " + String.format(Locale.CHINA, "%.2f", diff1Std)
                                + "\n二阶微分的均值: " + String.format(Locale.CHINA, "%.2f", diff2Mean) +
                                " 二阶微分的中值 " + String.format(Locale.CHINA, "%.2f", diff2Median) +
                                "\n二阶微分的方差: " + String.format(Locale.CHINA, "%.2f", diff2Std) +
                                "\n最大值比: " + String.format(Locale.CHINA, "%.2f", max_ratio) +
                                " 最小值比: " + String.format(Locale.CHINA, "%.2f", min_ratio) +
                                "\n" + String.format(Locale.CHINA, "%.2f", meanf) +
                                " " + String.format(Locale.CHINA, "%.2f", medianf) +
                                " " + String.format(Locale.CHINA, "%.2f", stdf) +
                                " " + "\n" + String.format(Locale.CHINA, "%.2f", varf) +
                                " " + String.format(Locale.CHINA, "%.2f", rmsf) +
                                " " + "\n" + String.format(Locale.CHINA, "%.2f", diff1Meanf) +
                                " " + String.format(Locale.CHINA, "%.2f", diff1Median) +
                                " " + String.format(Locale.CHINA, "%.2f", diff1Stdf) +
                                " " + "\n" + String.format(Locale.CHINA, "%.2f", diff2Meanf) +
                                " " + String.format(Locale.CHINA, "%.2f", diff2Median) +
                                " " + String.format(Locale.CHINA, "%.2f", diff2Stdf) +
                                " " + "\n" + String.format(Locale.CHINA, "%.2f", min_ratiof) +
                                " " + String.format(Locale.CHINA, "%.2f", min_ratiof) + " ";

                        //结束计算向handler发消息
                        handler.sendEmptyMessage(0);

                    }
                }.start();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //初始化图表
        initChart(mChart);
        mChart.clear();
        initChartData(); //初始化图表数据
    }

    private void initChartData() {
        //清除x轴和y轴的所有数据
        xval.clear();
        yval.clear();
        //读取csv文件的数据
        readCsvData(xval, yval, selectedFileName);
        //生成数据
        lineData = getLineData(xval, yval);
        //显示数据
        showChart(lineData);
    }

    //读取csv文件信息
    public void readCsvData(List<String> xval, List<Double> yval, String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            //前10行为杂项信息
            for (int i = 0; i < 10; i++) {
                line = br.readLine();
            }
            while ((line = br.readLine()) != null) {
                String[] dataList = line.split(",");
                xval.add(dataList[1]);
                yval.add(Double.parseDouble(dataList[2]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // region 绘图方法
    public LineData getLineData(List<String> xVals, List<Double> yvals) { //制作LineData
        ArrayList<Entry> yVals = new ArrayList<>();

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
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        // 创建折线数据对象（第二个参数可以是set）
        LineData lineData = new LineData(xVals, dataSets);
        lineData.setDrawValues(false);
        lineData.setValueTextColor(Color.BLACK);
        lineData.setValueTextSize(9f);

        return lineData;
    }

    private void initChart(LineChart mChart) {
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
    // endregion

    //消息回调
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //关闭ProgressDialog
            pd.dismiss();

            //更新UI
            textViewOutput.setText(str);
            //showDialog();
            Intent intent = new Intent();
            intent.setClass(DetectRecordAnalyseActivity.this, ResultActivity.class);
            Bundle bundle1 = new Bundle();
            Bundle bundle2 = new Bundle();

            bundle1.putSerializable("mean", mean);
            bundle2.putSerializable("peakNumbers", peakNumbers);

            intent.putExtras(bundle1);
            intent.putExtras(bundle2);

            startActivity(intent);
            return true;
        }
    });


    // region 数据计算方法
    public List<Double> getYfft(List<Double> y) {
        if (y.size() == 0) {
            return new ArrayList<>();
        }
        Complex[] num = new Complex[y.size()];
        for (int i = 0; i < y.size(); i++) {
            num[i] = new Complex(y.get(i), 0);
            //System.out.println(num[i]);
        }
        //System.out.println("After fft");
        Complex[] res = Complex.fft(num);
        List<Double> normalizeY = new ArrayList<>();//单边频谱只取一半
        double N = res.length / 2.0;
        for (int i = 0; i < (int) N; i++) {
            if (i == 0) {
                normalizeY.add(res[i].abs() / (N * 2));
                continue;
            }
            normalizeY.add(res[i].abs() / N);
        }
        return normalizeY;
    }

    public double[] getMinMax(List<Double> y) {
        if (y.size() == 0) {
            return new double[]{-1, -1};
        }
        double[] res = new double[2];
        res[0] = y.get(0);
        res[1] = res[0];
        for (int i = 0; i < y.size(); i++) {
            if (y.get(i) < res[0]) {
                res[0] = y.get(i);
            } else if (y.get(i) > res[1]) {
                res[1] = y.get(i);
            }
        }
        return res;
    }

    public double getSum(List<Double> y) {
        double sum = 0;
        for (int i = 0; i < y.size(); i++) {
            sum += y.get(i);
        }
        return sum;
    }

    public double getMean(List<Double> y, double sum) {
        if (y.size() == 0) {
            return -1;
        }
        return sum / y.size();
    }

    public double getVar(List<Double> y, double mean) {
        if (y.size() == 0) {
            return -1;
        }
        double res = 0;
        for (double i : y) {
            res += (i - mean) * (i - mean);
        }
        return res / y.size();
    }

    public double getStd(List<Double> y, double mean) {
        if (y.size() == 0) {
            return -1;
        }
        return Math.sqrt(getVar(y, mean));
    }

    public double getMedian(List<Double> y) {
        if (y.size() == 0) {
            return -1;
        }
        List<Double> res = new ArrayList<>(y);
        Collections.sort(res);
        if (res.size() % 2 == 0) {
            return (res.get(res.size() / 2) + res.get(res.size() / 2 - 1)) / 2;
        }
        return res.get(res.size() / 2);
    }

    public double getRms(List<Double> y) {
        if (y.size() == 0) {
            return -1;
        }
        double sum = 0;
        for (double i : y) {
            sum += i * i;
        }
        sum = Math.sqrt(sum / y.size());
        return sum;
    }

    public List<Double> getDiff(List<Double> y) {
        List<Double> diff = new ArrayList<>();
        if (y.size() == 0 || y.size() == 1) {
            return y;
        }
        for (int i = 1; i < y.size(); i++) {
            diff.add(y.get(i) - y.get(i - 1));
        }
        return diff;
    }

    public boolean equalsDouble(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    public int findPeaks(List<Double> src, List<Integer> indMax, List<Integer> indMin, double minPeakProminence) { //MinPeakProminence最小突出度
        if (src.size() == 0) {
            return 0;
        }
        int[] sign = new int[src.size()];
        for (int i = 1; i < src.size(); i++) {
            if (equalsDouble(src.get(i), src.get(i - 1))) {
                sign[i - 1] = 0;
            } else if (src.get(i) > src.get(i - 1)) {
                sign[i - 1] = 1;
            } else {
                sign[i - 1] = -1;
            }
        }
        for (int j = 1; j < src.size() - 1; j++) {  //寻找所有峰
            if (sign[j] < 0 && sign[j - 1] > 0) {
                indMax.add(j);
            } else if (sign[j] == 0) {
                if (sign[j + 1] < 0) {
                    indMax.add(j);
                }
            }
        }
        List<Integer> indMaxx = new ArrayList<>();
        for (int i = 0; i < indMax.size(); i++) {  //寻找突出度符合条件的峰
            double leftend = src.get(0);
            double rightend = src.get(src.size() - 1);
            double temp = src.get(indMax.get(i));
            int left = 0;
            int right = src.size() - 1;
            for (int j = i - 1; j >= 0; j--) {
                if (src.get(indMax.get(j)) > temp) {
                    left = indMax.get(j);
                    break;
                }
            }
            for (int j = i + 1; j < indMax.size(); j++) {
                if (src.get(indMax.get(j)) > temp) {
                    right = indMax.get(j);
                    break;
                }
            }

            leftend = src.get(indMax.get(i));
            for (int j = left; j < indMax.get(i); j++) {
                leftend = Math.min(leftend, src.get(j));
            }


            rightend = src.get(indMax.get(i));
            for (int j = indMax.get(i); j <= right; j++) {
                rightend = Math.min(rightend, src.get(j));
            }

            if (temp - leftend >= minPeakProminence && temp - rightend >= minPeakProminence) {
                indMaxx.add(indMax.get(i));
            }
        }
        indMax.clear();
        indMax.add(indMaxx.get(0));
        for (int i = 1; i < indMaxx.size(); i++) {
            indMax.add(indMaxx.get(i));
        }
        return indMax.size();

    }

    public List<Double> normalLize(List<Double> src) {
        if (src.size() == 0) {
            return src;
        }
        List<Double> res = new ArrayList<>();
        double max = Collections.max(src);
        double min = Collections.min(src);
        for (double d : src) {
            res.add((d - min) / (max - min));
        }
        return res;
    }
    // endregion
}
