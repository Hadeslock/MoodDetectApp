package com.example.pc.lbs.controller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.activity.DeviceMeasureActivity;
import com.example.pc.lbs.adapter.KeyTimeAdapter;
import com.example.pc.lbs.adapter.ScannedDeviceAdapter;
import com.example.pc.lbs.fragment.AppendCommentDialogFragment;
import com.example.pc.lbs.fragment.KeyTimeDialogFragment;
import com.example.pc.lbs.module.ReceivedData;
import com.example.pc.lbs.pojo.Patient;
import com.example.pc.lbs.utils.DateUtil;
import com.example.pc.lbs.utils.FileUtils;
import com.example.pc.lbs.utils.LineChartUtil;
import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.pc.lbs.utils.FileUtils.baseDirPath;

public class DeviceMeasureController {

    private DeviceMeasureActivity mView;

    private static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private ScannedDeviceAdapter mScannedDeviceAdapter;

    private ReceivedData receivedData;

    private boolean isMeasure = false;
    private boolean isScanning = false;

    //定位相关
    private LocationClient mLocationClient;
    double latitude;  //纬度信息
    double longitude;  //经度信息

    //病人相关
    private Patient selectPatient;

    //测试相关
    private List<String> keyTimeList = new ArrayList<>(); //关键时间点集合
    private KeyTimeAdapter mKeyTimeAdapter; //关键时间点数据代理
    private String fileLocalStore; //存储在本地的数据文件
    private String mComment; // 测试结束后添加的备注

    private boolean adaptiveChart; //是否自适应图表

    public DeviceMeasureController(DeviceMeasureActivity deviceMeasureActivity) {
        mView = deviceMeasureActivity;
        adaptiveChart = ((CheckBox) mView.findViewById(R.id.cb_adaptive_chart)).isChecked();

        initBle();  //初始化ble
        initAdapter();  //初始化列表适配器
        initLocation(); //初始化定位组件
    }

    //初始化fastble
    private void initBle() {
        BleManager.getInstance().init(mView.getApplication());
        //全局配置
        BleManager.getInstance().enableLog(true)    //配置日志
                .setReConnectCount(1, 5000)     //配置重连次数和重连间隔（毫秒）
                .setOperateTimeout(5000);   //设置readRssi、setMtu、write、read、notify、indicate的超时时间（毫秒）
    }

    //初始化列表适配器
    private void initAdapter() {
        mScannedDeviceAdapter = new ScannedDeviceAdapter();
        //设置扫描到的设备的事件监听
        mScannedDeviceAdapter.setOnScannedDeviceClickListener(new ScannedDeviceAdapter.OnScannedDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                //连接到设备
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(BleDevice bleDevice) {
                //与设备断开连接
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }
        });

        mKeyTimeAdapter = new KeyTimeAdapter(keyTimeList);
        mKeyTimeAdapter.setOnKeyTimeClickListener(bundle -> {
            //开弹窗
            DialogFragment keyTimeDialogFragment = new KeyTimeDialogFragment();
            keyTimeDialogFragment.setArguments(bundle);
            keyTimeDialogFragment.show(mView.getSupportFragmentManager(), "keyTimeNote");
        });


    }

    //设置扫描设备列表的数据源
    public void loadScannedDeviceList(ListView scannedDeviceLV) {
        scannedDeviceLV.setAdapter(mScannedDeviceAdapter);
    }


    //连接到设备
    private void connect(final BleDevice bleDevice) {
        ProgressDialog progressDialog = new ProgressDialog(mView);
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                progressDialog.dismiss();
                mView.connectFail();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                //清除进度条弹窗
                progressDialog.dismiss();
                //数据适配器添加数据
                mScannedDeviceAdapter.addDevice(bleDevice);
                mScannedDeviceAdapter.notifyDataSetChanged();
                //订阅设备通知
                BleManager.getInstance().notify(bleDevice, SERVICE_UUID, NOTIFY_UUID, new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {

                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {

                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        if (data != null && data.length > 0) {
                            final StringBuilder stringBuilder = new StringBuilder(data.length);
                            for (byte byteChar : data)
                                stringBuilder.append(String.format("%02X ", byteChar));
                            String potentialStr = new String(data);
                            // 蓝牙传输的包如果完整，应该以b开头，并以a结尾
                            if (potentialStr.startsWith("b") && potentialStr.endsWith("a")) {
                                //处理数据,去掉开头的b和结尾的a
                                potentialStr = potentialStr.substring(1, potentialStr.length() - 1);
                                if (isMeasure) {
                                    //设置数据
                                    int index = mScannedDeviceAdapter.getIndex(bleDevice);//获取编号
                                    if (index >= 0) {
                                        receivedData.setPotentialList(index, potentialStr);
                                    }
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                //设备适配器删除设备
                mScannedDeviceAdapter.removeDevice(bleDevice);
                mScannedDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    //初始化图表
    public void initChart(LineChart chart) {
        LineChartUtil.initChart(chart);
    }

    //更改扫描状态
    public void changeScanStatus() {
        if (!isScanning) {
            //未在扫描
            setScanRule(); //设置扫描过滤
            startScan(); //开始扫描
        } else {
            //停止扫描
            BleManager.getInstance().cancelScan();
        }
        //更改扫描状态标志位
        isScanning = !isScanning;
    }

    //设置扫描过滤
    private void setScanRule() {
        UUID[] uuids = {UUID.fromString(SERVICE_UUID)};
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder().setServiceUuids(uuids)      // 只扫描指定的服务的设备，可选
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    //开始扫描并设置回调
    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                //清除扫描的设备
                mScannedDeviceAdapter.clearUnconnectedDevice();
                mScannedDeviceAdapter.notifyDataSetChanged();
                //设置界面扫描状态
                mView.setScanStatus(true);
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                //扫描到设备
                mScannedDeviceAdapter.addDevice(bleDevice); //添加设备
                mScannedDeviceAdapter.notifyDataSetChanged(); //订阅设备数据通知
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                isScanning = false;
                mView.setScanStatus(false);
            }
        });
    }

    //改变测量状态
    public void changeMeasureStatus() {
        if (!isMeasure) {
            startMeasure();
            mView.setMeasureStatus(true);
        } else {
            stopMeasure();
            mView.setMeasureStatus(false);
        }
        isMeasure = !isMeasure;
    }

    //开始测量
    public void startMeasure() {
        //检查病人和设备是否选择
        if (selectPatient == null || mScannedDeviceAdapter.getCount() == 0) {
            Toast.makeText(mView, "请选择病人和设备", Toast.LENGTH_SHORT).show();
        } else {
            //清除未连接的设备
            mScannedDeviceAdapter.clearUnconnectedDevice();
            mScannedDeviceAdapter.notifyDataSetChanged();
            //设置接收数据，回调监听数据变化
            receivedData = new ReceivedData(mScannedDeviceAdapter.getCount(), datalist -> {
                LineChartUtil.showDataInChart(mView.findViewById(R.id.chart), datalist, adaptiveChart);
                //如果未测量就直接返回,不把数据存到本地文件中，只是显示图表
                if (!isMeasure) return;
                List<String> localData = new ArrayList<>(datalist);
                //添加时间信息
                localData.add(0, DateUtil.getNowTime());
                localData.add(0, DateUtil.getNowDate());
                //保存至本地文件
                FileUtils.addLineToCsvFile(baseDirPath, fileLocalStore, localData);
            });
            //设置开始时间标签
            mView.setTimeTag(true);
            //进行一些文件的初始化工作
            initFileStore();
            //失效开始测试按钮，使能添加关键点和结束测试按钮
            mView.findViewById(R.id.btn_mark_key_point).setEnabled(true);
            //开启前台通知
            mView.startBLEForegroundService();
        }
    }

    //结束测量
    public void stopMeasure() {
        //断开所有连接
        BleManager.getInstance().disconnectAllDevice();
        if (isMeasure) {
            //设置结束时间标签
            mView.setTimeTag(false);
            //关闭前台通知
            mView.stopBLEForegroundService();
            //失效添加关键点按钮，使能添加备注按钮
            mView.findViewById(R.id.btn_mark_key_point).setEnabled(false);
            mView.findViewById(R.id.btn_append_comment).setEnabled(true);
            //将关键点信息写入测试记录文件
            if (keyTimeList.size() == 0) {
                keyTimeList.add("null");
            }
            FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, keyTimeList, 2, FileUtils.REPLACE);
        }
    }

    //一些文件的初始化工作
    private void initFileStore() {
        //初始化存储在本地的数据文件
        File filePath = new File(FileUtils.baseDirPath);
        File[] files = filePath.listFiles(pathname -> {
            String fileName = pathname.getName();
            return fileName.startsWith(DateUtil.getNowDate());
        });
        int idx = files.length + 1;
        fileLocalStore = DateUtil.getNowDate() + "_" + idx + "_tbs.csv";
        //添加测试文件的基本信息
        addBasicInfo();
    }

    //添加测试文件的基本信息
    private void addBasicInfo() {
        //文件开头空10行出来
        ArrayList<String> fileHeader = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            fileHeader.add("\n");
        }
        FileUtils.addLineToCsvFile(baseDirPath, fileLocalStore, fileHeader);
        //添加经纬度信息 第一行
        ArrayList<String> coordinate = new ArrayList<>();
        coordinate.add("latitude");
        coordinate.add(String.valueOf(latitude));
        coordinate.add("longitude");
        coordinate.add(String.valueOf(longitude));
        FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, coordinate, 1, FileUtils.REPLACE);
        //添加设备信息 第四行
        ArrayList<String> deviceInfo = new ArrayList<>();
        for (int i = 0; i < mScannedDeviceAdapter.getCount(); i++) {
            BleDevice device = mScannedDeviceAdapter.getItem(i);
            deviceInfo.add(device.getName());
            deviceInfo.add(device.getMac());
        }
        FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, deviceInfo, 4, FileUtils.REPLACE);
        //添加病人信息 第五行
        ArrayList<String> patientInfo = new ArrayList<>();
        patientInfo.add(String.valueOf(selectPatient.getId()));
        patientInfo.add(selectPatient.getName());
        patientInfo.add(String.valueOf(selectPatient.getAge()));
        patientInfo.add(String.valueOf(selectPatient.getGender()));
        patientInfo.add(selectPatient.getPosition());
        patientInfo.add(selectPatient.getIdentity());
        FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, patientInfo, 5, FileUtils.REPLACE);
    }

    //清除图像
    public void clearChart(LineChart chart) {
        //初始化图表
        initChart(chart);
        //重置所有缩放和拖动并使图表完全适合它的边界
        chart.fitScreen();
    }

    //获取到病人数据
    public void setPatient(@NonNull Intent data) {
        selectPatient = data.getParcelableExtra(DeviceMeasureActivity.EXTRAS_SELECTED_PATIENT);
        mView.setSelectedPatient(selectPatient.getName());
    }

    //添加关键时间点
    public void addKeyTime() {
        String curTime = DateUtil.getNowTime();
        //在时间点集合添加数据
        keyTimeList.add(curTime);
        mKeyTimeAdapter.notifyItemInserted(keyTimeList.size() - 1);
    }

    //设置是否自适应显示
    public void setAdaptiveChart(boolean isChecked) {
        adaptiveChart = isChecked;
    }

    //添加备注
    public void appendComment() {
        //传递当前的备注
        Bundle bundle = new Bundle();
        bundle.putString("comment", mComment);
        //开弹窗
        AppendCommentDialogFragment dialogFragment = new AppendCommentDialogFragment();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(mView.getSupportFragmentManager(), "appendComment");
    }

    //设置关键时间点列表数据适配器
    public void loadKeyTimeList(RecyclerView keyTimeRV) {
        keyTimeRV.setAdapter(mKeyTimeAdapter);
    }

    //更新关键时间点的信息
    public void updateKeyTime(int position, String note) {
        //接收到修改后的关键时间点描述，更新对应的数据
        keyTimeList.set(position, note);
        mKeyTimeAdapter.notifyItemChanged(position);
        //更新本地文件
        FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, keyTimeList, 2, FileUtils.REPLACE);
    }

    //保存备注并添加到文件
    public void updateComment(String comment) {
        mComment = comment;
        List<String> comments = new ArrayList<String>() {{
            add(comment);
        }};
        boolean appendResult = FileUtils.addDataToSpecifiedLineOfCsv(baseDirPath, fileLocalStore, comments, 3, FileUtils.REPLACE);
        if (appendResult) {
            Toast.makeText(mView, "更新备注成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mView, "更新备注失败", Toast.LENGTH_SHORT).show();
        }
    }

    //定位监听
    public class myLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            latitude = bdLocation.getLatitude();    //获取纬度信息
            longitude = bdLocation.getLongitude();    //获取经度信息
        }
    }

    //初始化定位服务
    private void initLocation() {
        //声明LocationClient类
        mLocationClient = new LocationClient(mView.getApplicationContext());
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
}
