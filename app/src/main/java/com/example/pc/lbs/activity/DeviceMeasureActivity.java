package com.example.pc.lbs.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.blankj.utilcode.util.StringUtils;
import com.example.pc.lbs.R;
import com.example.pc.lbs.service.BLEReadService;
import com.example.pc.lbs.service.BluetoothLeService;
import com.example.pc.lbs.utils.DateUtil;
import com.example.pc.lbs.utils.FileUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.pc.lbs.utils.FileUtils.baseDirPath;

/**
 * Author: Hadeslock
 * Created on 2022/4/29 16:14
 * Email: hadeslock@126.com
 * Desc: 设备测量的活动
 */
public class DeviceMeasureActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DeviceMeasureActivity.class.getSimpleName();

    //请求跳转扫描设备界面，本活动的标识码
    public static final String INTENT_SCAN_DEVICE_FOR_MEASURE = TAG + "INTENT_SCAN_DEVICE_FOR_MEASURE";

    // 请求码
    private static final int ACTION_SELECT_DEVICE = 1; //选择设备请求码
    private static final int ACTION_SELECT_PATIENT = 2; //选择病人请求码

    //活动间传递数据的key
    public static final String EXTRAS_SELECTED_DEVICE = "SelectedDevice"; //选择的设备
    public static final String EXTRAS_SELECTED_PATIENT_ID = "SelectedPatientId"; //选择的病人id
    public static final String EXTRAS_SELECTED_PATIENT_NAME = "SelectedPatientName"; //选择的病人姓名

    //消息代码
    private static final int MSG_DISCONNECT_UNEXPECTED = 1;

    private final int MAX_VISIBLE_COUNT = 300; //图表最多显示的x范围
    private final int MAX_DRAW_COUNT = 3000; //图表最多画多少数据量

    // region 界面组件引用
    private Button connectDeviceBtn; //连接设备按钮
    private Button selectPatientBtn; //选择病人按钮
    private Button clearTimeBtn; //清除时间戳按钮
    private Button clearImageBtn; //清除图像按钮
    private Button startTestBtn; //用于记录开始测试时间
    private Button keyTimeBtn; //用于记录关键时间点的按钮
    private Button endTestBtn; //记录结束测试时间按钮
    private Button uploadRecordBtn; //上传测试记录的按钮
    private TextView deviceConnectStatus; //设备连接状态内容标签
    private TextView selectedPatientTextView; //选择的病人显示标签
    private LineChart mChart; //绘图区
    private TextView startTime; //开始时间时间戳
    private TextView keyTimeTextView; //关键时间点时间戳
    private TextView endTime; //结束时间时间戳
    // endregion

    //服务和特征值
    private BluetoothGattCharacteristic notify_characteristic;

    //蓝牙相关
    private BluetoothLeService mBluetoothLeService; //蓝牙ble服务
    private BluetoothDevice selectDevice; //选择的蓝牙设备
    private String mDeviceName; //蓝牙设备名称
    private String mDeviceAddress; //蓝牙设备地址

    //病人相关
    private int selectPatientId;
    private String selectPatientName;

    //定位相关
    private LocationClient mLocationClient;
    double latitude;  //纬度信息
    double longitude;  //经度信息

    //测试相关
    private boolean mConnected = false; //连接状态
    private boolean mMeasuring = false; //测量状态
    private int keyTimeIdx = 0; //关键时间点编号
    private List<String> keyTimeList = new ArrayList<>(); //关键时间点集合
    public static String fileToBeSend; //要发送给服务器的文件名
    private String fileLocalStore; //存储在本地的数据文件

    //活动创建的钩子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_measure);

        initView(); //初始化界面引用
        initLocation(); //初始化定位组件

        //绑定蓝牙服务，同时连接到蓝牙服务器
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    //活动可见的钩子
    @Override
    protected void onResume() {
        super.onResume();
        //初始化图表
        initChart();

        //动态获取权限
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //注册广播监听
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        //如果无法连接到蓝牙服务器，报错
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //解除广播监听
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //断开连接
        mBluetoothLeService.disconnect();
        //解绑蓝牙服务
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        //解除前台通知
        stopBLEForegroundService();
    }

    //点击事件回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.clear_image == id) { //清图像按钮
            initChart();
            //重置所有缩放和拖动并使图表完全适合它的边界
            mChart.fitScreen();
        } else if (R.id.button_start_test == id) { //开始测试按钮
            //检查病人和设备是否选择
            if (StringUtils.isEmpty(selectPatientName) || selectDevice == null) {
                Toast.makeText(this, "请选择病人和设备", Toast.LENGTH_SHORT).show();
            } else {
                //设置标签
                String startTimeTag = "开始时间：" + DateUtil.getNowTime();
                startTime.setText(startTimeTag);
                //设置测量状态
                mMeasuring = true;
                //进行一些文件的初始化工作
                initFileStore();
                //开启前台通知
                startBLEForegroundService();
            }

        } else if (R.id.button_end_test == id) { //结束测试按钮
            //设置标签
            String endTimeTag = "结束时间：" + DateUtil.getNowTime();
            endTime.setText(endTimeTag);
            //设置测量状态
            mMeasuring = false;
            //断开蓝牙连接
            mBluetoothLeService.disconnect();
            //关闭前台通知
            stopBLEForegroundService();
            //将关键点信息写入测试记录文件
            if (keyTimeList.size() == 0) {
                keyTimeList.add("null");
            }
            FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, keyTimeList, 1, 0);
            FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileToBeSend, keyTimeList, 2, 0);
        } else if (R.id.button_clear_time == id) { //清除时间按钮
            startTime.setText("");
            endTime.setText("");
        } else if (R.id.measure_upload_btn == id) { //上传记录按钮
            //跳转上传测试记录页面
            Intent uploadIntent = new Intent(
                    DeviceMeasureActivity.this, ViewUnuploadRecordActivity.class);
            startActivity(uploadIntent);
            finish();
        } else if (R.id.measure_connect_device_btn == id) { //连接设备按钮
            //连接设备前先选择病人，不然会少信息
            if (StringUtils.isEmpty(selectPatientName)) {
                Toast.makeText(this, "请先选择病人", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, ScanDeviceActivity.class);
                intent.putExtra("fromActivity", INTENT_SCAN_DEVICE_FOR_MEASURE);
                startActivityForResult(intent, ACTION_SELECT_DEVICE);
            }
        } else if (R.id.measure_select_patient_btn == id) { //选择病人按钮
            Intent intent = new Intent(this, SelectPatientActivity.class);
            startActivityForResult(intent, ACTION_SELECT_PATIENT);
        } else if (R.id.button_mark_key_point == id) { //关键时间点按钮
            String curTime = DateUtil.getNowTime();
            //在时间点集合添加数据
            keyTimeList.add(curTime);
            //在时间戳添加信息
            keyTimeTextView.append("关键点" + (++keyTimeIdx) + ":" + curTime + "\n");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_SELECT_DEVICE == requestCode && RESULT_OK == resultCode) {
            //选择设备成功
            assert data != null;
            selectDevice = data.getParcelableExtra(EXTRAS_SELECTED_DEVICE);
            mDeviceName = selectDevice.getName();
            mDeviceAddress = selectDevice.getAddress();
        } else if (ACTION_SELECT_PATIENT == requestCode && RESULT_OK == resultCode) {
            //选择病人成功
            assert data != null;
            selectPatientId = data.getIntExtra(EXTRAS_SELECTED_PATIENT_ID, -1);
            selectPatientName = data.getStringExtra(EXTRAS_SELECTED_PATIENT_NAME);
            selectedPatientTextView.setText(selectPatientName); //设置已选择的病人姓名
        }
    }

    //消息回调
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (MSG_DISCONNECT_UNEXPECTED == what) {
                //连接意外断开
                //弹窗提示
                new AlertDialog.Builder(DeviceMeasureActivity.this)
                        .setTitle("提示")
                        .setMessage("连接意外断开，是否立即上传测试记录？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //上传测试记录
                                Intent uploadIntent = new Intent(
                                        DeviceMeasureActivity.this, ViewUnuploadRecordActivity.class);
                                startActivity(uploadIntent);
                            }
                        })
                        .show();
            }
            return false;
        }
    });

    // 用于管理服务生命周期的代码
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //初始化服务
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // 启动初始化成功后自动连接设备。
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // 通知接收器，处理服务触发的各种事件。
    // ACTION_GATT_CONNECTED: 连接到 GATT 服务器。
    // ACTION_GATT_DISCONNECTED: 与 GATT 服务器断开连接。
    // ACTION_GATT_SERVICES_DISCOVERED: 发现 GATT 服务。
    // ACTION_DATA_AVAILABLE: 从设备接收到的数据。这可能是读取或通知操作的结果。
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) { //连接到GATT 服务器
                //设置连接状态
                mConnected = true;
                String connectInfo = mDeviceName + " 已连接";
                deviceConnectStatus.setText(connectInfo);
                //使能几个测试按钮
                startTestBtn.setEnabled(true);
                keyTimeBtn.setEnabled(true);
                endTestBtn.setEnabled(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //与 GATT 服务器断开连接
                //设置连接状态
                mConnected = false;
                deviceConnectStatus.setText("未连接");
                //如果是在测量的时候断开，就提示一下
                if (mMeasuring) {
                    Message message = new Message();
                    message.what = MSG_DISCONNECT_UNEXPECTED;
                    mHandler.sendMessage(message);
                    mMeasuring = false;
                }
                //初始化空白UI
                //initUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //发现 GATT 服务
                // 初始化服务和特征值,通过Android拿得到对应Service和Characteristic的UUID.
                initServiceAndChara();
                //订阅蓝牙测量结果特征通知
                mBluetoothLeService.setCharacteristicNotification(notify_characteristic, true);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //从设备接收到数据
                String potentialStr = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                // 蓝牙传输的包如果完整，应该以b开头，并以a结尾
                if (potentialStr.startsWith("b") && potentialStr.endsWith("a")) {
                    //处理数据,去掉开头的b和结尾的a
                    potentialStr = potentialStr.substring(1, potentialStr.length() - 1);
                    //绘图
                    addChartEntry(potentialStr);
                    //如果未测量就直接返回
                    if (!mMeasuring) return;
                    //格式化数据
                    List<String> dataList = new ArrayList<>();
                    dataList.add(DateUtil.getNowDate());
                    dataList.add(DateUtil.getNowTime());
                    dataList.add(potentialStr);
                    //保存至本地文件
                    FileUtils.addLineToCsvFile(baseDirPath, fileLocalStore, dataList);
                    //保存至发送到云服务器的文件，因为和本地数据格式不同
                    FileUtils.addLineToCsvFile(baseDirPath, fileToBeSend, dataList);
                }
            }
        }
    };

    //通过Android拿得到对应Service和Characteristic的UUID.
    private void initServiceAndChara() {
        List<BluetoothGattService> bluetoothGattServices = mBluetoothLeService.getSupportedGattServices();
        for (BluetoothGattService gattService : bluetoothGattServices) {
            List<BluetoothGattCharacteristic> characteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    notify_characteristic = characteristic;
                    Log.d(TAG, "notify_chara_uuid = " + notify_characteristic.getUuid());
                }
            }
        }
    }

    //初始化界面组件
    private void initView() {
        //初始化组件引用
        connectDeviceBtn = findViewById(R.id.measure_connect_device_btn);
        selectPatientBtn = findViewById(R.id.measure_select_patient_btn);
        clearImageBtn = findViewById(R.id.clear_image);//清除图像
        startTestBtn = findViewById((R.id.button_start_test));
        keyTimeBtn = findViewById(R.id.button_mark_key_point);
        endTestBtn = findViewById((R.id.button_end_test));
        clearTimeBtn = findViewById(R.id.button_clear_time);
        uploadRecordBtn = findViewById(R.id.measure_upload_btn);
        selectedPatientTextView = findViewById(R.id.measure_selected_patient);
        deviceConnectStatus = findViewById(R.id.device_connect_status);
        startTime = findViewById((R.id.start_time));
        keyTimeTextView = findViewById(R.id.key_time);
        endTime = findViewById((R.id.end_time));
        mChart = findViewById(R.id.chart);

        //设置点击事件
        connectDeviceBtn.setOnClickListener(this);
        selectPatientBtn.setOnClickListener(this);
        clearImageBtn.setOnClickListener(this);
        startTestBtn.setOnClickListener(this);
        keyTimeBtn.setOnClickListener(this);
        endTestBtn.setOnClickListener(this);
        clearTimeBtn.setOnClickListener(this);
        uploadRecordBtn.setOnClickListener(this);
    }

    //初始化定位服务
    private void initLocation() {
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(new myLocationListener());
        //定位选项
        LocationClientOption option = new LocationClientOption();
        //可选，设置定位模式，默认高精度
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //option.setCoorType("bd09ll");
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        //option.setScanSpan(1000);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(option);
        //启动服务
        mLocationClient.start();
    }

    public class myLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            latitude = bdLocation.getLatitude();    //获取纬度信息
            longitude = bdLocation.getLongitude();    //获取经度信息
            Log.i(TAG, "获取到经纬度信息: " + latitude + " - " + longitude);
        }
    }

    //一些文件的初始化工作
    private void initFileStore() {
        //初始化存储在本地的数据文件
        File filePath = new File(FileUtils.baseDirPath);
        File[] files = filePath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName = pathname.getName();
                return fileName.startsWith(DateUtil.getNowDate());
            }
        });
        int idx = files.length + 1;
        fileLocalStore = DateUtil.getNowDate() + "_" + idx + ".csv";
        //初始化要发送给服务器的文件名.tbs-to be sent
        fileToBeSend = "tbs" + mDeviceName + DateUtil.getNowDateTime() + ".csv";
        FileUtils.makeFilePath(baseDirPath, fileToBeSend);
        //添加设备信息、病人信息、经纬度信息
        ArrayList<String> dataInfo = new ArrayList<>();
        dataInfo.add("deviceMac");
        dataInfo.add(mDeviceAddress);
        dataInfo.add("patientId");
        dataInfo.add(String.valueOf(selectPatientId));
        dataInfo.add("latitude");
        dataInfo.add(String.valueOf(latitude));
        dataInfo.add("longitude");
        dataInfo.add(String.valueOf(longitude));
        FileUtils.addLineToCsvFile(baseDirPath, fileToBeSend, dataInfo);
    }

    //开启前台服务提醒的函数。
    private void startBLEForegroundService() {
        Intent startIntent = new Intent(this, BLEReadService.class);
        startService(startIntent);
    }

    //关闭前台服务提醒的函数
    private void stopBLEForegroundService() {
        Intent stopIntent = new Intent(this, BLEReadService.class);
        stopService(stopIntent);
    }

    //广播接收过滤器，只接收蓝牙服务发送的广播
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // region ---------------------- 绘图方法开始 -------------------------
    // reference: https://weeklycoding.com/mpandroidchart-documentation/
    //初始化图表
    private void initChart() {
        // 初始化图表的各项设置
        initChartSetting();
        // 初始化图表数据
        LineData lineData = new LineData();
        // 给图表设置一个空数据，后面再动态添加
        mChart.setData(lineData);
        //刷新图表
        mChart.invalidate();
    }

    //初始化图表的各项设置
    private void initChartSetting() {
        // 设置描述
        mChart.setDescription("动态折线图");
        // 设置可触摸
        mChart.setTouchEnabled(true);
        // 可拖曳
        mChart.setDragEnabled(true);

        //mChart.setDragDecelerationFrictionCoef(0);
        // 可缩放
        mChart.setScaleEnabled(true);
        //mChart.setAutoScaleMinMaxEnabled(false);
        // 设置绘制网格背景
        mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(true);
        // 设置图表的背景颜色
        mChart.setBackgroundColor(0xfff5f5f5);
        // 图表注解（只有当数据集存在时候才生效）
        Legend legend = mChart.getLegend();
        // 设置图表注解部分的位置
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        // 线性，也可是圆
        legend.setForm(Legend.LegendForm.LINE);
        // 颜色
        legend.setTextColor(Color.BLUE);
        // x坐标轴
        XAxis xl = mChart.getXAxis();
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
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(0xff37474f);
        // 最大值
        // leftAxis.setAxisMaxValue(100f);  //改为100
        // 最小值
        //   leftAxis.setAxisMinValue(-140f);
        // 不一定要从0开始
        // leftAxis.setStartAtZero(true);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = mChart.getAxisRight();
        // 不显示图表的右边y坐标轴线
        rightAxis.setEnabled(false);

        // 警戒线
        LimitLine ll = new LimitLine(70f, "警戒线");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        leftAxis.addLimitLine(ll);
        //mChart.setVisibleXRangeMaximum(2);
        mChart.setVisibleXRangeMaximum(10);
    }

    // 为曲线添加一个坐标点
    // reference: https://blog.csdn.net/zhangphil/article/details/50185115?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0.pc_relevant_antiscanv2&spm=1001.2101.3001.4242.1&utm_relevant_index=3
    private void addChartEntry(String potentialStr) {
        // 获取图表数据
        LineData lineData = mChart.getData();
        // 每一个LineDataSet代表一条线，每张统计图表可以同时存在若干个统计折线，这些折线像数组一样从0开始下标
        // 这里只有一条电位折线数据，所以是第0条
        LineDataSet dataSet = lineData.getDataSetByIndex(0);
        //检查数据集有没有，没有就创建一个
        if (dataSet == null) {
            dataSet = createLineDataSet();
            lineData.addDataSet(dataSet);
        }
        //校验数据集的量是否超过了指定的最大绘图数据量,超过了就移出数据集的第一个数据
        //做这样的限制是因为长时间测量产生的数据可能会导致手机内存不足
        // reference: https://stackoverflow.com/questions/44537353/how-can-i-remove-old-data-in-mpandroidchart
        int entryCount = dataSet.getEntryCount();
        if (entryCount > MAX_DRAW_COUNT) {
            dataSet.removeFirst();
            for (Entry yVal : dataSet.getYVals()) {
                yVal.setXIndex(yVal.getXIndex() - 1);
            }
            lineData.removeXValue(0);
        }
        // 添加横坐标值
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss ", Locale.CHINA); //时间格式化
        String time = dateFormat.format(new Date());
        lineData.addXValue(time + "");
        // 增加电位数据
        //设置数据点entry
        float potential = Float.parseFloat(potentialStr);
        Entry entry = new Entry(potential, entryCount);
        //Entry entry = new Entry(potential, entryCount);
        // 往linedata里面添加点。注意：addentry的第二个参数即代表折线的下标索引。
        // 因为这里只有一个统计折线，那么就是第一个，其下标为0.
        // 如果同一张统计图表中存在若干条统计折线，那么必须分清是针对哪一条（依据下标索引）统计折线添加。
        lineData.addEntry(entry, 0);
        //数据更新
        lineData.notifyDataChanged();
        // 像ListView那样的通知数据更新
        mChart.notifyDataSetChanged();
        // 当前统计图表中最多在x轴坐标线上显示的总量
        mChart.setVisibleXRangeMaximum(MAX_VISIBLE_COUNT);
        mChart.moveViewToX(lineData.getXValCount() - MAX_VISIBLE_COUNT);
        mChart.invalidate(); //fresh
    }

    /*
     * 初始化一个曲线数据集
     * @author hadeslock
     * @date 2022/4/20 15:11
     * @param data 数据集的数据
     * @return LineDataSet 初始化的曲线数据集
     */
    private LineDataSet createLineDataSet() {
        LineDataSet set = new LineDataSet(null, "电位差");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //设置线条的格式
        set.setColor(Color.RED);
        set.setCircleColor(Color.rgb(125, 0, 0));
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
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                            ViewPortHandler viewPortHandler) {
                DecimalFormat decimalFormat = new DecimalFormat(".0");
                return decimalFormat.format(value);
            }
        });
        return set;
    }
    // endregion ---------------------- 绘图方法结束 -------------------------


}