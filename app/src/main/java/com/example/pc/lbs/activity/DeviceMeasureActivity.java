package com.example.pc.lbs.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pc.lbs.R;
import com.example.pc.lbs.controller.DeviceMeasureController;
import com.example.pc.lbs.fragment.AppendCommentDialogFragment;
import com.example.pc.lbs.fragment.KeyTimeDialogFragment;
import com.example.pc.lbs.service.BLEReadService;
import com.example.pc.lbs.utils.DateUtil;
import com.github.mikephil.charting.charts.LineChart;

/**
 * Author: Hadeslock
 * Created on 2022/4/29 16:14
 * Email: hadeslock@126.com
 * Desc: 设备测量的活动
 */
public class DeviceMeasureActivity extends AppCompatActivity
        implements View.OnClickListener, KeyTimeDialogFragment.KeyTimeDialogListener,
        AppendCommentDialogFragment.AppendCommentDialogListener {
    private static final String TAG = DeviceMeasureActivity.class.getSimpleName();

    //请求跳转扫描设备界面，本活动的标识码
    public static final String INTENT_SCAN_DEVICE_FOR_MEASURE = TAG + "INTENT_SCAN_DEVICE_FOR_MEASURE";

    // 请求码
    private static final int ACTION_SELECT_PATIENT = 2; //选择病人请求码

    //活动间传递数据的key
    public static final String EXTRAS_SELECTED_DEVICE = "SelectedDevice"; //选择的设备
    public static final String EXTRAS_SELECTED_PATIENT = "SelectedPatient"; //选择的病人

    // region 界面组件引用
    private Button scanDeviceBtn; //连接设备按钮
    private Button selectPatientBtn; //选择病人按钮
    private Button appendCommentBtn; //添加备注按钮
    private Button clearImageBtn; //清除图像按钮
    private Button measureBtn; //测量按钮，更改测量状态，添加开始结束时间
    private Button keyTimeBtn; //用于记录关键时间点的按钮
    private Button uploadRecordBtn; //上传测试记录的按钮
    private TextView selectedPatientTextView; //选择的病人显示标签
    private ListView scannedDeviceLV; //扫描到的设备显示列表
    private LineChart mChart; //绘图区
    private TextView startTime; //开始时间时间戳
    private RecyclerView keyTimeRV; //关键时间点列表
    private TextView endTime; //结束时间时间戳
    private CheckBox adaptiveChartCB; //自适应图表单选框
    // endregion

    private DeviceMeasureController mController;

    //活动创建的钩子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_measure);

        initView(); //初始化界面引用
        initEvent(); //初始化组件时间

        mController = new DeviceMeasureController(this);
    }

    //初始化界面组件
    private void initView() {
        //初始化组件引用
        scanDeviceBtn = findViewById(R.id.btn_scan_device);
        selectPatientBtn = findViewById(R.id.measure_select_patient_btn);
        clearImageBtn = findViewById(R.id.clear_image);//清除图像
        measureBtn = findViewById((R.id.btn_measure));
        keyTimeBtn = findViewById(R.id.btn_mark_key_point);
        appendCommentBtn = findViewById(R.id.btn_append_comment);
        uploadRecordBtn = findViewById(R.id.measure_upload_btn);
        selectedPatientTextView = findViewById(R.id.measure_selected_patient);
        scannedDeviceLV = findViewById(R.id.lv_scanned_device);
        startTime = findViewById((R.id.start_time));
        keyTimeRV = findViewById(R.id.rv_key_time);
        endTime = findViewById((R.id.end_time));
        mChart = findViewById(R.id.chart);
        adaptiveChartCB = findViewById(R.id.cb_adaptive_chart);

        //设置线性视图
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        keyTimeRV.setLayoutManager(layoutManager);

    }

    //初始化事件
    private void initEvent() {
        //设置点击事件
        scanDeviceBtn.setOnClickListener(this);
        selectPatientBtn.setOnClickListener(this);
        clearImageBtn.setOnClickListener(this);
        measureBtn.setOnClickListener(this);
        keyTimeBtn.setOnClickListener(this);
        appendCommentBtn.setOnClickListener(this);
        uploadRecordBtn.setOnClickListener(this);

        adaptiveChartCB.setOnCheckedChangeListener((buttonView, isChecked) -> mController.setAdaptiveChart(isChecked));
    }

    //活动可见的钩子
    @Override
    protected void onResume() {
        super.onResume();
        //动态获取权限
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //初始化图表
        mController.initChart(mChart);
        mController.loadScannedDeviceList(scannedDeviceLV);
        mController.loadKeyTimeList(keyTimeRV);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除前台通知
        stopBLEForegroundService();
        //结束测量
        mController.stopMeasure();
    }

    //点击事件回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.clear_image == id) { //清图像按钮
            mController.clearChart(mChart);
        } else if (R.id.btn_measure == id) { //测试按钮
            mController.changeMeasureStatus();
        } else if (R.id.btn_append_comment == id) { //添加备注按钮
            mController.appendComment();
        } else if (R.id.measure_upload_btn == id) { //上传记录按钮
            //跳转上传测试记录页面
            Intent uploadIntent = new Intent(
                    DeviceMeasureActivity.this, ViewUnuploadRecordActivity.class);
            startActivity(uploadIntent);
            finish();
        } else if (R.id.btn_scan_device == id) { //扫描设备按钮
            mController.changeScanStatus();
        } else if (R.id.measure_select_patient_btn == id) { //选择病人按钮
            Intent intent = new Intent(this, SelectPatientActivity.class);
            startActivityForResult(intent, ACTION_SELECT_PATIENT);
        } else if (R.id.btn_mark_key_point == id) { //关键时间点按钮
            mController.addKeyTime();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_SELECT_PATIENT == requestCode && RESULT_OK == resultCode) {
            //选择病人成功
            assert data != null;
            mController.setPatient(data);
        }
    }

    //关键时间点点击后弹出的对话框的监听回调
    @Override
    public void onDialogPositiveClick(int position, String note) {
        mController.updateKeyTime(position, note);
    }

    //添加备注按钮点击后弹出的对话框的监听回调
    @Override
    public void onDialogPositiveClick(String comment) {
        mController.updateComment(comment);
    }


    //开启前台服务提醒的函数。
    public void startBLEForegroundService() {
        Intent startIntent = new Intent(this, BLEReadService.class);
        startService(startIntent);
    }

    //关闭前台服务提醒的函数
    public void stopBLEForegroundService() {
        Intent stopIntent = new Intent(this, BLEReadService.class);
        stopService(stopIntent);
    }

    /*
     * 设置测量按钮状态
     * @author hadeslock
     * @date 2022/5/19 15:44
     * @param isMeasuring 是否在测量
     * @return void
     */
    public void setMeasureStatus(boolean isMeasuring) {
        measureBtn.setText(isMeasuring ? R.string.stop_measure : R.string.start_measure);
    }

    //设置已选择的病人姓名
    public void setSelectedPatient(String selectPatientName) {
        selectedPatientTextView.setText(selectPatientName);
    }

    /*
     * 设置扫描按钮状态
     * @author hadeslock
     * @date 2022/5/19 15:46
     * @param isScanning 是否在扫描
     * @return void
     */
    public void setScanStatus(boolean isScanning) {
        scanDeviceBtn.setText(isScanning ? R.string.stop_scan : R.string.start_scan);
    }


    public void connectFail() {
        scanDeviceBtn.setText(R.string.start_scan);
        Toast.makeText(this, "连接失败", Toast.LENGTH_LONG).show();
    }

    /*
     * 设置开始或结束时间标记
     * @author hadeslock
     * @date 2022/5/23 15:44
     * @param start true-设置开始时间标记 false-设置结束时间标记
     * @return void
     */
    public void setTimeTag(boolean start) {
        String timeTag = (start ? "开始时间：" : "结束时间：") + DateUtil.getNowTime();
        if (start) {
            startTime.setText(timeTag);
        } else {
            endTime.setText(timeTag);
        }
    }
}