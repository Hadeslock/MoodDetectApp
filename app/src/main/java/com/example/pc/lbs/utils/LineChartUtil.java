package com.example.pc.lbs.utils;

import android.graphics.Color;
import com.example.pc.lbs.module.extremumQueue;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author: Hadeslock
 * Created on 2022/5/18 13:53
 * Email: hadeslock@126.com
 * Desc: 折线图绘图工具类
 * reference: <a href="https://weeklycoding.com/mpandroidchart-documentation/">...</a>
 */

public class LineChartUtil {

    //绘图相关
    private static final int MAX_VISIBLE_COUNT = 200; //图表最多显示的x范围
    private static final int MAX_DRAW_COUNT = 600; //图表最多画多少数据量

    private static final int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.GRAY, Color.CYAN};

    private static extremumQueue<Float> shownData = new extremumQueue<>(); //显示在图表上的数据范围


    //初始化图表
    public static void initChart(LineChart chart) {
        // 初始化图表的各项设置
        initChartSetting(chart);
        // 给图表设置一个空数据，后面再动态添加
        LineData lineData = new LineData();
        chart.setData(lineData);
        //刷新图表
        chart.invalidate();
        //清除时间窗数据
        shownData = new extremumQueue<>();
    }

    //初始化图表设置
    private static void initChartSetting(LineChart chart) {
        // 设置描述
        chart.setDescription("动态折线图");
        // 设置可触摸
        chart.setTouchEnabled(true);
        // 可拖曳
        chart.setDragEnabled(true);

        //chart.setDragDecelerationFrictionCoef(0);
        // 可缩放
        chart.setScaleEnabled(true);
        //chart.setAutoScaleMinMaxEnabled(false);
        // 设置绘制网格背景
        chart.setDrawGridBackground(true);
        chart.setPinchZoom(true);
        // 设置图表的背景颜色
        chart.setBackgroundColor(0xfff5f5f5);
        // 图表注解（只有当数据集存在时候才生效）
        Legend legend = chart.getLegend();
        // 设置图表注解部分的位置
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        // 线性，也可是圆
        legend.setForm(Legend.LegendForm.LINE);
        // 颜色
        legend.setTextColor(Color.BLUE);
        // x坐标轴
        XAxis xl = chart.getXAxis();
        xl.setTextColor(0xff00897b);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        // 几个x坐标轴之间才绘制
        xl.setSpaceBetweenLabels(1);
        // 如果false，那么x坐标轴将不可见
        xl.setEnabled(true);
        // 将X坐标轴放置在底部，默认是在顶部。
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置x轴坐标为时间


        // 图表左边的y坐标轴线
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(0xff37474f);
        // 最大值
        // leftAxis.setAxisMaxValue(100f);  //改为100
        // 最小值
        //   leftAxis.setAxisMinValue(-140f);
        // 不一定要从0开始
        // leftAxis.setStartAtZero(true);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = chart.getAxisRight();
        // 不显示图表的右边y坐标轴线
        rightAxis.setEnabled(false);

        // 警戒线
        LimitLine ll = new LimitLine(70f, "警戒线");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        leftAxis.addLimitLine(ll);
        //chart.setVisibleXRangeMaximum(2);
        chart.setVisibleXRangeMaximum(10);
    }


    public static void showDataInChart(LineChart chart, List<String> datalist, boolean adaptive) {
        addXValue(chart);
        int size = datalist.size();
        for (int i = 0; i < size; i++) {
            //绘图
            addChartEntry(chart, datalist.get(i), i);
        }
        constraintChart(chart);
        //维护显示窗口的数据
        for (String potentialStr : datalist) {
            shownData.pushLast(Float.valueOf((potentialStr)));
        }
        if (shownData.size() > (MAX_VISIBLE_COUNT * size)) {
            for (int i = 0; i < size; i++) {
                shownData.popFirst();
            }
        }
        //自适应显示
        YAxis axisLeft = chart.getAxisLeft();
        if (adaptive) {
            float max = shownData.max(), min = shownData.min();
            axisLeft.setStartAtZero(false);
            axisLeft.setAxisMaxValue(max + 3);
            axisLeft.setAxisMinValue(min - 3);
        } else {
            axisLeft.resetAxisMaxValue();
            axisLeft.resetAxisMinValue();
        }

    }

    //校验数据集的量是否超过了指定的最大绘图数据量,超过了就移出数据集的第一个数据
    //做这样的限制是因为长时间测量产生的数据可能会导致手机内存不足
    // reference: https://stackoverflow.com/questions/44537353/how-can-i-remove-old-data-in-mpandroidchart
    private static void constraintChart(LineChart chart) {
        // 获取图表数据
        LineData lineData = chart.getData();
        int dataSetCount = lineData.getDataSetCount();
        boolean overflow = lineData.getDataSetByIndex(0).getEntryCount() > MAX_DRAW_COUNT;
        //约束y
        for (int i = 0; i < dataSetCount; i++) {
            LineDataSet dataSet = lineData.getDataSetByIndex(i);
            if (overflow) {
                dataSet.removeFirst();
                for (Entry yVal : dataSet.getYVals()) {
                    yVal.setXIndex(yVal.getXIndex() - 1);
                }
            }
        }
        //约束x
        if (overflow) {
            lineData.removeXValue(0);
        }
        //数据更新
        lineData.notifyDataChanged();
        // 像ListView那样的通知数据更新
        chart.notifyDataSetChanged();
    }

    // 为曲线添加一个坐标点
    // reference: https://blog.csdn.net/zhangphil/article/details/50185115?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0.pc_relevant_antiscanv2&spm=1001.2101.3001.4242.1&utm_relevant_index=3
    public static void addChartEntry(LineChart chart, String potentialStr, int dataSetIndex) {
        // 获取图表数据
        LineData lineData = chart.getData();
        // 每一个LineDataSet代表一条线，每张统计图表可以同时存在若干个统计折线，这些折线像数组一样从0开始下标
        LineDataSet dataSet = lineData.getDataSetByIndex(dataSetIndex);
        //检查数据集有没有，没有就创建一个
        if (dataSet == null) {
            dataSet = createLineDataSet(dataSetIndex);
            lineData.addDataSet(dataSet);
        }
        // 增加电位数据
        //设置数据点entry
        int entryCount = dataSet.getEntryCount();
        float potential = Float.parseFloat(potentialStr);
        Entry entry = new Entry(potential, entryCount);
        // 往linedata里面添加点。注意：addentry的第二个参数即代表折线的下标索引。
        // 如果同一张统计图表中存在若干条统计折线，那么必须分清是针对哪一条（依据下标索引）统计折线添加。
        lineData.addEntry(entry, dataSetIndex);
        //数据更新
        lineData.notifyDataChanged();
        // 像ListView那样的通知数据更新
        chart.notifyDataSetChanged();
        // 当前统计图表中最多在x轴坐标线上显示的总量
        chart.setVisibleXRangeMaximum(MAX_VISIBLE_COUNT);
        chart.moveViewToX(lineData.getXValCount() - MAX_VISIBLE_COUNT);
        chart.invalidate(); //fresh
    }

    // 为曲线添加一个横坐标
    public static void addXValue(LineChart chart) {
        // 获取图表数据
        LineData lineData = chart.getData();
        // 添加横坐标值
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ", Locale.CHINA); //时间格式化
        String time = dateFormat.format(new Date());
        lineData.addXValue(time);
        //数据更新
        lineData.notifyDataChanged();
        // 像ListView那样的通知数据更新
        chart.notifyDataSetChanged();
    }

    /*
     * 初始化一个曲线数据集
     * @author hadeslock
     * @date 2022/4/20 15:11
     * @param data 数据集的数据
     * @return LineDataSet 初始化的曲线数据集
     */
    private static LineDataSet createLineDataSet(int index) {
        LineDataSet set = new LineDataSet(null, "电位差");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //设置线条的格式
        set.setColor(colors[index]);
        set.setCircleColor(colors[index] & 0xFFC0C0C0);
        set.setLineWidth(2f);
        set.setCircleSize(2f);
        set.setFillAlpha(45);

        //设置曲线值的圆点是实心还是空心
        set.setDrawCircleHole(false);
        set.setValueTextSize(10f);

        //设置折线图填充
        set.setDrawFilled(false);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);
        //设置数据的格式
        set.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
            DecimalFormat decimalFormat = new DecimalFormat(".0");
            return decimalFormat.format(value);
        });
        return set;
    }
}
