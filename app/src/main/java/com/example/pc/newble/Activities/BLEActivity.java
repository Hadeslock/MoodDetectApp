package com.example.pc.newble.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.blankj.utilcode.util.ToastUtils;
import com.example.pc.newble.BLEReadService;
import com.example.pc.newble.R;
import com.example.pc.newble.TheUtils.DateUtil;
import com.example.pc.newble.TheUtils.FileUtils;
import com.example.pc.newble.TheUtils.HexUtil;
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
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import com.example.pc.newble.SQLite.*;

public class BLEActivity extends AppCompatActivity {

    private static final String TAG ="ble_tag" ;

    private MyDBHandler dbHandler;

    // 储存最后一次有效的地址
    private String currentAddressStr;
    private Double currentLongittude;
    private Double currentLatitude;

    ImageView ivSerBleStatus;
    TextView tvSerBleStatus;
    TextView tvSerBindStatus;
    ListView bleListView;
    private LinearLayout operaView;
    private Button btnWrite;
    private Button btnRead;           //按键用来读取数据等
    private Button startService;
    private Button stopService;
    private Button saveImage;

    private Button startForegroundService;
    private Button stopForegroundService ;

    private EditText etWriteContent;
    private TextView tvResponse;      //写出数据
    private List<BluetoothDevice> mDatas;
    private List<Integer> mRssis;
    private com.example.pc.newble.adapter.BleAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean isScaning=false;
    private boolean isConnecting=false;
    public BluetoothGatt mBluetoothGatt;//改为public
    private String datautf8;
    private float  dataview;//用于画图
    private String j;
    private Button reconnect;//用于发现蓝牙断开后重连

    private String gettvResponse;

    //服务和特征值
    private UUID write_UUID_service;
    private UUID write_UUID_chara;
    private UUID read_UUID_service;
    private UUID read_UUID_chara;
    private UUID notify_UUID_service;
    private UUID notify_UUID_chara;
    private UUID indicate_UUID_service;
    private UUID indicate_UUID_chara;
    private String hex="7B46363941373237323532443741397D";

    //引入作图所需的代码
    // 高温线下标
    private final int HIGH = 0;
    // 低温线下标
    private final int LOW = 1;

    private LineChart mChart;

    //引入定位所需要的代码
    public LocationClient mLocationClientble;

    private TextView positionTextble;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        //下面部分为前台服务测试用
        startForegroundService = findViewById(R.id.button_start_service);
        stopForegroundService = findViewById(R.id.button_stop_service);
        startForegroundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBLEForegroundService();
            }
        });
        stopForegroundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBLEForegroundService();
            }
        });

        // 初始化
        initView();
        initData();
        requestRxPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);//申请权限
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//判断蓝牙是否打开没打开的话，隐式调用打开系统开启蓝牙
            startActivityForResult(intent,0);
        }
        //作图所需
        mChart = (LineChart) findViewById(R.id.chart);
        showChart(getLineData());

        // 实例化 dpHandler。
        try {
            dbHandler = new MyDBHandler(this, null, null, 1);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.i(TAG, errors.toString());
        }


        // 标记今天日期和时间
        FileUtils.addStringToFile(getSDCardPath() + "/bletest/Datalist.txt", "\n");    //datalist
        FileUtils.addStringToFile(getSDCardPath() + "/bletest/Datalist.txt", DateUtil.getNowDateTime().substring(0, 8));


        //定位所需
        mLocationClientble = new LocationClient(getApplicationContext());
        mLocationClientble.registerLocationListener(new MyLocationListenerble());
        positionTextble = (TextView) findViewById(R.id.position_text_viewble);
        positionTextble.setMovementMethod(ScrollingMovementMethod.getInstance());
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(BLEActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(BLEActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(BLEActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(BLEActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

    }


    //定位需要方法
    private void requestLocation() {
        initLocation();
        mLocationClientble.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClientble.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListenerble implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
          //  currentPosition.append("时间：").append(location.getTime()).append("\n");
         //   currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
          //  currentPosition.append("经线：").append(location.getLongitude()).append("\n");
          //  currentPosition.append("国家：").append(location.getCountry()).append("\n");
          //  currentPosition.append("省：").append(location.getProvince()).append("\n");
          //  currentPosition.append("市：").append(location.getCity()).append("\n");
          //  currentPosition.append("区：").append(location.getDistrict()).append("\n");
          //  currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("地址： ").append(location.getAddrStr()).append("\n");// 地址信息

            String string = location.getAddrStr();
            Log.e(TAG, "onReceiveLocation: 尝试定位：" + string + "||||" + (string.equals("null")));

            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }

            // 如果地址无效，则沿袭上一个有效地址，如果有效则更新
            if (string.equals("null") != true ) {
                currentAddressStr = location.getAddrStr();
                currentLongittude = location.getLongitude();
                currentLatitude = location.getLatitude();
                positionTextble.setText(currentPosition);
            }


        }

    }


    //作图需要方法
    /**
     * 显示图表
     */
    private void showChart(LineData lineData) {
        // 初始化图表
        initChart();
        // 数据显示的颜色
        lineData.setValueTextColor(Color.BLACK);
        // 给图表设置数据
        mChart.setData(lineData);
    }

    /**
     * 获取折线数据
     *
     * @return
     */
    private LineData getLineData() {
        // 创建折线数据
        LineData lineData = new LineData();
        // 添加数据集
        lineData.addDataSet(getHighLineDataSet());
        //  lineData.addDataSet(getLowLineDataSet());
        // 返回折线数据
        return lineData;
    }

    /**
     * 初始化图表
     */
    private void initChart() {
        // 设置描述
        mChart.setDescription("动态折线图");
        // 设置可触摸
        mChart.setTouchEnabled(true);
        // 可拖曳
        mChart.setDragEnabled(true);
        // 可缩放
        mChart.setScaleEnabled(true);
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
        xl.setSpaceBetweenLabels(5);
        // 如果false，那么x坐标轴将不可见
        xl.setEnabled(true);
        // 将X坐标轴放置在底部，默认是在顶部。
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        // 图表左边的y坐标轴线
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(0xff37474f);
        // 最大值
        leftAxis.setAxisMaxValue(50f);
        // 最小值
     //   leftAxis.setAxisMinValue(-140f);
        // 不一定要从0开始
        leftAxis.setStartAtZero(true);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = mChart.getAxisRight();
        // 不显示图表的右边y坐标轴线
        rightAxis.setEnabled(false);

        // 警戒线
        LimitLine ll = new LimitLine(40f, "警戒线");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(2f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        leftAxis.addLimitLine(ll);

    }


    // 为高温线和低温线添加一个坐标点
    private void addChartEntry( float dataview) {
        // 获取图表数据
        LineData lineData = mChart.getData();
        // 添加横坐标值
        lineData.addXValue((lineData.getXValCount()) + "");

        // 增加高温
        LineDataSet highLineDataSet = lineData.getDataSetByIndex(HIGH);//?
        float high = dataview;//将high改为 dataview
        Entry entryHigh = new Entry(high, highLineDataSet.getEntryCount());
        lineData.addEntry(entryHigh, HIGH);

        // 增加低温
      /*  LineDataSet lowLineDataSet = lineData.getDataSetByIndex(LOW);
        float low = (float) ((Math.random()) * 10);
        Entry entryLow = new Entry(low, lowLineDataSet.getEntryCount());
        lineData.addEntry(entryLow, LOW);*/

        // 使用新数据刷新图表
        mChart.notifyDataSetChanged();

        // 当前统计图表中最多在x轴坐标线上显示的总量
        mChart.setVisibleXRangeMaximum(12);

        mChart.moveViewToX(lineData.getXValCount() - 12);
    }

    // 初始化数据集，添加一条高温统计折线
    private LineDataSet getHighLineDataSet() {
        LineDataSet set = new LineDataSet(null, "电位差");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        // 折线的颜色
        set.setColor(Color.RED);
        set.setCircleColor(Color.YELLOW);
        set.setLineWidth(2f);
        set.setCircleSize(8f);
        set.setFillAlpha(128);
        set.setCircleColorHole(Color.BLUE);
        set.setHighLightColor(Color.GREEN);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(10f);
     //   set.setDrawValues(true);



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




        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                            ViewPortHandler viewPortHandler) {
                DecimalFormat decimalFormat = new DecimalFormat(".0");
                String s =  decimalFormat.format(value);
                return s;
            }
        });

        return set;
    }



    /**
     * 给图表添加数据
     *
     * @param
     */


    //作图需要方法结束
    //蓝牙方法开始

    private void initData() {
        mDatas=new ArrayList<>();
        mRssis=new ArrayList<>();
        mAdapter=new com.example.pc.newble.adapter.BleAdapter(BLEActivity.this,mDatas,mRssis);
        bleListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void initView(){     //引用控件资源

        ivSerBleStatus=findViewById(R.id.iv_ser_ble_status);
        tvSerBindStatus=findViewById(R.id.tv_ser_bind_status);//绑定状态
        tvSerBleStatus=findViewById(R.id.tv_ser_ble_status);
        bleListView=findViewById(R.id.ble_list_view);
        operaView=findViewById(R.id.opera_view);
        // btnWrite=findViewById(R.id.btnWrite);
        // btnRead=findViewById(R.id.btnRead);
        startService=findViewById(R.id.start_service);
        stopService=findViewById(R.id.stop_service);
        saveImage=findViewById(R.id.save_image);
        // etWriteContent=findViewById(R.id.et_write);
        tvResponse=findViewById(R.id.tv_response);
        tvResponse.setMovementMethod(ScrollingMovementMethod.getInstance());//滚动
        tvResponse.setGravity(Gravity.BOTTOM);//滚到最后一行
        reconnect= findViewById(R.id.reconnect_ble);



      /*  btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
            }
        });*/

       /* btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行写入操作
                writeData();
            }
        });*/
        startService.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {
               /* if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                i++;
                j=String.valueOf( i);
                j=j+".txt";
                FileUtils.writeTxtToFile(datautf8, "/sdcard/1/", j);
                Log.d(TAG, "savedata ");
                if(i>1000){
                    i=0;
                }}*/
               /* i++;
                j=String.valueOf( i);
                j="/h"+j+".txt";*/

                String   str   = "/"+ DateUtil.getNowDateTime()+".txt"; //文件名

                File sdCardDir = Environment.getExternalStorageDirectory();
                String sdcarddir = getSDCardPath()+"/bletest";    //地址
                gettvResponse = tvResponse.getText().toString();
                FileUtils.writeTxtToFile(gettvResponse, sdcarddir, str);
                Log.d(TAG, "savedata ");

            }
        });
        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String   str   = DateUtil.getNowDateTime();
                String sdcarddir = getSDCardPath()+"/bletest";
                mChart.saveToPath("title" + System.currentTimeMillis(), "/bletest");

            }
        });
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvResponse.setText(null);
            }
        });


        ivSerBleStatus.setOnClickListener(new View.OnClickListener() {//这里可改为button
            @Override
            public void onClick(View v) {
                if (isScaning){
                    tvSerBindStatus.setText("停止搜索");
                    stopScanDevice();
                }else{
                    checkPermissions();
                }

            }
        });
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled()){
                    Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//判断蓝牙是否打开没打开的话，隐式调用打开系统开启蓝牙
                    startActivityForResult(intent,0);
                }
                mBluetoothGatt.close();
                bleListView.setVisibility(View.VISIBLE);//设备列表消失
                operaView.setVisibility(View.GONE);//读取数据的列表出现
                ivSerBleStatus.setVisibility(View.VISIBLE);

                tvSerBindStatus.setVisibility(View.VISIBLE);
                tvSerBindStatus.setText("未连接");

                // 关闭前台服务
                stopBLEForegroundService();
            }
        });
        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//？？？？
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isScaning){
                    stopScanDevice();
                }
                if (!isConnecting){
                    isConnecting=true;
                    BluetoothDevice bluetoothDevice= mDatas.get(position);
                    //连接设备
                    tvSerBindStatus.setText("连接中");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断了一下手机系统，6.0及以上连接设备的方法
                        // 是bluetoothDevice.connectGatt(MainActivity.this,true, gattCallback, TRANSPORT_LE)。
                        mBluetoothGatt = bluetoothDevice.connectGatt(BLEActivity.this,
                                true, gattCallback, TRANSPORT_LE);
                    } else {
                        mBluetoothGatt = bluetoothDevice.connectGatt(BLEActivity.this,
                                true, gattCallback);
                    }
                }

            }
        });


    }

    private void readData() {
        BluetoothGattCharacteristic characteristic=mBluetoothGatt.getService(read_UUID_service)
                .getCharacteristic(read_UUID_chara);
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * 开始扫描 10秒后自动停止
     * */
    private void scanDevice(){
        tvSerBindStatus.setText("正在搜索");
        isScaning=true;
        //进度条出现
        mBluetoothAdapter.startLeScan(scanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //结束扫描
                mBluetoothAdapter.stopLeScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isScaning=false;
                        //进度条消失
                        tvSerBindStatus.setText("搜索已结束");
                    }
                });
            }
        },10000);
    }

    /**
     * 停止扫描
     * */
    private void stopScanDevice(){
        isScaning=false;
        //进度条消失
        mBluetoothAdapter.stopLeScan(scanCallback);
    }


    BluetoothAdapter.LeScanCallback scanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.e(TAG, "run: scanning...");
            if (!mDatas.contains(device)){
                mDatas.add(device);
                mRssis.add(rssi);
                mAdapter.notifyDataSetChanged();
            }

        }
    };

    private BluetoothGattCallback gattCallback=new BluetoothGattCallback() {   //回调方法
        /**
         * 断开或连接 状态发生变化时调用
         * */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG,"onConnectionStateChange()");
            if (status==BluetoothGatt.GATT_SUCCESS){
                //连接成功
                if (newState== BluetoothGatt.STATE_CONNECTED){
                    Log.e(TAG,"连接成功");
                    //发现服务
                    gatt.discoverServices();
                }
            }else{
                //连接失败
                Log.e(TAG,"失败=="+status);
                mBluetoothGatt.close();
                isConnecting=false;
            }
        }
        /**
         * 发现设备（真正建立连接）
         * */
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //直到这里才是真正建立了可通信的连接
            isConnecting=false;
            Log.e(TAG,"onServicesDiscovered()---建立连接");
            //获取初始化服务和特征值
            initServiceAndChara();
            //订阅通知//////////////////通过Android拿得到对应Service和Characteristic的UUID.
            mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt
                    .getService(notify_UUID_service).getCharacteristic(notify_UUID_chara),true);


            runOnUiThread(new Runnable() {//线程
                @Override
                public void run() {
                    bleListView.setVisibility(View.GONE);//设备列表消失
                    operaView.setVisibility(View.VISIBLE);//读取数据的列表出现
                    ivSerBleStatus.setVisibility(View.GONE);
                    tvSerBleStatus.setVisibility(View.GONE);
                    tvSerBindStatus.setVisibility(View.GONE);
                    tvSerBindStatus.setText("已连接");
                    // 启动 foreground service
                    startBLEForegroundService();

                }
            });
        }

        /**
         * 读操作的回调
         * */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e(TAG,"onCharacteristicRead()");
        }
        /**
         * 写操作的回调
         * */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.e(TAG,"onCharacteristicWrite()  status="+status+",value="+ HexUtil.encodeHexStr(characteristic.getValue()));
        }

        /**
         * 接收到硬件返回的数据，并将它们写入数据库
         * */
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG,"onCharacteristicChanged()"+characteristic.getValue());
            // data：接收到的二进制数据
            final byte[] data = characteristic.getValue();
            datautf8 = toStringHex1(bytes2hex(data));
            // 清除蓝牙传回数据前面的修饰符
            datautf8 = datautf8.substring(9);
            datautf8 = datautf8.replaceAll("[a-zA-Z]","" );  //^[0-9]+ [+-*\] [0-9]
            dataview = Float.parseFloat(datautf8);

            // 电压取绝对值
            dataview = Math.abs(dataview);

            try {
                Log.e(TAG, "onCharacteristicChanged: 哈哈哈哈零零落落" );
                // 如果要更改往数据库内传入的参数的话，务必在 Products.java 里面重写构造函数
                Log.e(TAG, "onCharacteristicChanged: 哈哈哈哈" );
                Products product = new Products(dataview, currentLongittude, currentLatitude,currentAddressStr);
                Log.e(TAG, "addButtonClicked: 哈哈哈哈哈哈哈" + product);
                dbHandler.addItem(product);
                Log.i(TAG, "Invoked: Add Item To Database. ");

                // 创建 csv 文件
                // 每次对csv读写前都要检查。makeFilePath 在文件已存在时不会报错，类似 python open 的w模式
                FileUtils.makeFilePath(FileUtils.getSDCardPath() + "/bletest/",
                        DateUtil.getNowDateTime().substring(0, 8) + ".csv");

                // 保存到 csv 文件
                Vector<String> string = new Vector<>();
                string.add(product.data);
                string.add(product.time);
                string.add(product.voltage);
                string.add(product.longitude);
                string.add(product.latitude);
                string.add(product.address);
                string.add(product.channel);

                FileUtils.addLineToCsvFile(getSDCardPath() + "/bletest/" + DateUtil.getNowDateTime().substring(0, 8) + ".csv", string);

            } catch (Exception e){
                e.printStackTrace();
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // addText(tvResponse,bytes2hex(data));//转成16进制
                    addText(tvResponse,datautf8);//转成16进制
                    addChartEntry(dataview);
                    // 存储入数据库

                }
            });

        }
    };

    /**
     * 检查权限
     */
    private void checkPermissions() {
        RxPermissions rxPermissions = new RxPermissions(BLEActivity.this);
        rxPermissions.request(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            // 用户已经同意该权限
                            scanDevice();
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUtils.showLong("用户开启权限后才能使用");
                        }
                    }
                });
     /*   RxPermissions rxPermissions1 = new RxPermissions(MainActivity.this);
        rxPermissions1.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            // 用户已经同意该权限
                            scanDevice();
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUtils.showLong("用户开启权限后才能使用");
                        }
                    }
                });*/
    }


    private void initServiceAndChara(){//通过Android拿得到对应Service和Characteristic的UUID.
        List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();
        for (BluetoothGattService bluetoothGattService:bluetoothGattServices){
            List<BluetoothGattCharacteristic> characteristics=bluetoothGattService.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic:characteristics){
                int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    read_UUID_chara=characteristic.getUuid();
                    read_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"read_chara="+read_UUID_chara+"----read_service="+read_UUID_service);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    write_UUID_chara=characteristic.getUuid();
                    write_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                    write_UUID_chara=characteristic.getUuid();
                    write_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);

                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    notify_UUID_chara=characteristic.getUuid();
                    notify_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"notify_chara="+notify_UUID_chara+"----notify_service="+notify_UUID_service);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    indicate_UUID_chara=characteristic.getUuid();
                    indicate_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"indicate_chara="+indicate_UUID_chara+"----indicate_service="+indicate_UUID_service);

                }
            }
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

    private void writeData(){
        BluetoothGattService service=mBluetoothGatt.getService(write_UUID_service);
        BluetoothGattCharacteristic charaWrite=service.getCharacteristic(write_UUID_chara);
        byte[] data;
        String content=etWriteContent.getText().toString();
        if (!TextUtils.isEmpty(content)){
            data= HexUtil.hexStringToBytes(content);
        }else{
            data= HexUtil.hexStringToBytes(hex);
        }
        if (data.length>20){//数据大于个字节 分批次写入
            Log.e(TAG, "writeData: length="+data.length);
            int num=0;
            if (data.length%20!=0){
                num=data.length/20+1;
            }else{
                num=data.length/20;
            }
            for (int i=0;i<num;i++){
                byte[] tempArr;
                if (i==num-1){
                    tempArr=new byte[data.length-i*20];
                    System.arraycopy(data,i*20,tempArr,0,data.length-i*20);
                }else{
                    tempArr=new byte[20];
                    System.arraycopy(data,i*20,tempArr,0,20);
                }
                charaWrite.setValue(tempArr);
                mBluetoothGatt.writeCharacteristic(charaWrite);
            }
        }else{
            charaWrite.setValue(data);
            mBluetoothGatt.writeCharacteristic(charaWrite);
        }
    }

    private static final String HEX = "0123456789abcdef";
    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt(b & 0x0f));
        }
        return sb.toString();

    }

    public static String toStringHex1(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                        i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return s;
    }



    private void requestRxPermissions(String... permissions) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(permissions).subscribe(new io.reactivex.functions.Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean granted) throws Exception {
                if (granted){
                    Toast.makeText(BLEActivity.this, "已获取权限", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BLEActivity.this, "已拒绝一个或以上权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 获得手机 SD 卡 的路径的函数
     * */
    private String getSDCardPath(){
        File sdcardDir = null;
        //判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdcardExist){
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }


    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothGatt!=null){
        mBluetoothGatt.disconnect();}
        mLocationClientble.stop();
        stopBLEForegroundService();
    }

    /**
     * 用于避免本 Activity 在按下返回键时被杀死的函数。
     * */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        Log.i(TAG, "Key_Status = 哈哈哈哈哈哈哈" + event.getAction());
        if (keycode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 右键处理
            moveTaskToBack(true); }
        return true;
    }

    /**
     * 开启前台服务提醒的函数。
     * 目前在两个地方被调用：测试用按钮被按下时；蓝牙成功连接时
     * */
    private void startBLEForegroundService(){
        Intent startIntent = new Intent(BLEActivity.this, BLEReadService.class);
        startService(startIntent);
    }

    /**
     * 关闭前台服务提醒的函数。
     * 目前在三个地方被调用：测试用按钮被按下时；蓝牙手动重新连接时；此Activity的onDestroy
     * TODO 在蓝牙因外力断开时调用本函数
     * */
    private void stopBLEForegroundService(){
        Intent stopIntent = new Intent(BLEActivity.this, BLEReadService.class);
        stopService(stopIntent);
    //    Toast.makeText(BLEActivity.this, "已经断开和蓝牙的连接", Toast.LENGTH_SHORT).show();
    }


}
