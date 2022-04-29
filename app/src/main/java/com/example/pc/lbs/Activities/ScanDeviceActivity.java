package com.example.pc.lbs.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.HttpUtil;
import com.example.pc.lbs.adapter.BleDeviceListAdapter;
import com.example.pc.lbs.pojo.Device;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/27 21:29
 * Email: hadeslock@126.com
 * Desc: 扫描设备的活动
 */
public class ScanDeviceActivity extends AppCompatActivity implements Handler.Callback {
    private static final String TAG = ScanDeviceActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1; //开启蓝牙请求码
    private static final long SCAN_PERIOD = 10000; //扫描时间，10秒后停止扫描

    //消息码
    private static final int MSG_LOCATION_DENIED = 1; //取消打开定位的消息
    private static final int MSG_NETWORK_FAILURE = 2; //网络错误
    private static final int MSG_RECEIVE_DEVICES = 3; //接收到绑定的设备数据
    private static final int MSG_REQUEST_DEVICE_FAILURE = 4; //请求绑定设备数据失败

    // ----------------- 界面组件引用 ----------------
    private ImageView bleSearchBtn;
    private TextView searchStatus;
    private ListView deviceList;

    // ----------------- 蓝牙组件 ------------------
    private BluetoothAdapter mBluetoothAdapter; //蓝牙适配器
    private BleDeviceListAdapter mBleDeviceListAdapter; //蓝牙设别列表数据适配器
    private BluetoothDevice mSelectedBluetoothDevice; //列表选择的蓝牙设备
    // 设备扫描回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (!mBleDeviceListAdapter.isDeviceExist(device)) {
                mBleDeviceListAdapter.addDevice(device);
                mBleDeviceListAdapter.addRssi(rssi);
                mBleDeviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    // ----------------- 定位组件 ----------------------
    private LocationManager mLocationManager;

    private List<Device> mBoundDeviceList; //当前登录用户绑定的设备列表

    private Handler mHandler; //消息回调
    private boolean mScanning = false; //是否在扫描的标志
    private static String fromActivity; //本活动是由那个活动发起的,注意这个参数必须通过intent传进来

    //活动创建的钩子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        mHandler = new Handler(this);

        Intent intent = getIntent();
        fromActivity = intent.getStringExtra("fromActivity");

        //初始化组件、适配器
        initView();
        initBleAdapter();
        initLocation();
        initBleListData();
    }

    //活动可见时的钩子
    @Override
    protected void onResume() {
        super.onResume();
        //确认蓝牙已开启，如果未开启，开一个弹窗请求开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //确认定位已开启，如未开启，开一个弹窗请求开启定位
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(this).setTitle("请开启定位服务")
                    .setMessage("要使用Ble设备扫描功能必须开启设备定位")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsIntent);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Message finishMsg = new Message();
                            finishMsg.what = MSG_LOCATION_DENIED;
                            mHandler.sendMessage(finishMsg);
                        }
                    })
                    .show();
        }
        initBleListData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //用户拒绝开启蓝牙，就结束活动
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "您已拒绝开启蓝牙，无法使用", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //消息回调
    @Override
    public boolean handleMessage(Message msg) {
        int what = msg.what;
        if (MSG_LOCATION_DENIED == what) { //用户拒绝开启定位
            Toast.makeText(this, "您已拒绝开启定位，无法使用", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (MSG_NETWORK_FAILURE == what) { //网络错误
            Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
            return true;
        } else if (MSG_RECEIVE_DEVICES == what) { //接收到绑定的设备数据
            boolean flag = false; //标志选择的设备是否已经绑定
            String address = mSelectedBluetoothDevice.getAddress();
            for (Device device : mBoundDeviceList) {
                if (address.equals(device.getDevice_id())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                Toast.makeText(this, "你选择的设备未绑定", Toast.LENGTH_SHORT).show();
            } else {
                //跳转到选择病人界面
                Intent intent = new Intent(ScanDeviceActivity.this, SelectPatientActivity.class);
                //传递参数
                intent.putExtra(DeviceMeasureActivity.EXTRAS_SELECTED_DEVICE, mSelectedBluetoothDevice);
                startActivity(intent);
                //结束活动
                finish();
            }
            return true;
        } else if (MSG_REQUEST_DEVICE_FAILURE == what) {
            //请求绑定设备数据失败
            Toast.makeText(this, "请求绑定设备数据失败", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    //活动暂停的钩子
    @Override
    protected void onPause() {
        super.onPause();
        scanBleDevice(false);
        mBleDeviceListAdapter.clear();
    }

    /*
     * 扫描设备
     * @author hadeslock
     * @date 2022/4/15 14:57
     * @param enable true-开始扫描 false-结束扫描
     * @return void
     */
    private void scanBleDevice(boolean enable) {
        if (enable) {
            // 开一个计时线程，指定时间后停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    searchStatus.setText("停止搜索");
                }
            }, SCAN_PERIOD);
            //更改状态
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /*
     * 初始化蓝牙设备列表适配器数据
     * @author Hadeslock
     * @time 2022/4/15 13:36
     */
    private void initBleListData() {
        //初始化数据适配器
        mBleDeviceListAdapter = new BleDeviceListAdapter(this);
        //设置数据适配器
        deviceList.setAdapter(mBleDeviceListAdapter);
        //订阅数据
        mBleDeviceListAdapter.notifyDataSetChanged();
    }

    //初始化组件
    private void initView() {
        //初始化组件引用
        bleSearchBtn = findViewById(R.id.iv_search_device);
        searchStatus = findViewById(R.id.tv_search_status);
        deviceList = findViewById(R.id.ble_device_list);

        //设置组件点击回调
        bleSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning) {
                    //正在扫描就停止扫描
                    searchStatus.setText("停止搜索");
                    scanBleDevice(false);
                } else {
                    mBleDeviceListAdapter.clear(); //先清空之前的数据
                    //开始扫描
                    searchStatus.setText("正在搜索");
                    scanBleDevice(true);
                }
            }
        });
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //如果扫描没有停止就先停止
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                //获取被点击的设备
                mSelectedBluetoothDevice = mBleDeviceListAdapter.getDevice(position);
                if (mSelectedBluetoothDevice == null) {
                    return;
                }
                Log.d(TAG, "onItemClick: " + mSelectedBluetoothDevice.getName() + "" + mSelectedBluetoothDevice.getAddress());
                //根据fromActivity执行相应的逻辑
                jumpActivity();
            }
        });
    }

    //根据fromActivity执行相应的逻辑
    private void jumpActivity() {
        if (MainActivity.INTENT_SCAN_DEVICE_FOR_MEASURE.equals(fromActivity)) {
            //从主菜单来的，下一步要跳转到测量界面
            //先判断一下设备是否被登录用户绑定
            String fetchDevicesURL = BuildConfig.baseUrl + "device/allDevices";
            HttpUtil.getRequest(fetchDevicesURL, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Message message = new Message();
                    message.what = MSG_NETWORK_FAILURE;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    int code = response.code();
                    Message message = new Message();
                    if (200 == code) { //响应成功
                        message.what = MSG_RECEIVE_DEVICES;
                        //解析返回的响应
                        mBoundDeviceList = Device.parseDeviceListFromResponse(response);
                    } else {
                        message.what = MSG_REQUEST_DEVICE_FAILURE;
                    }
                    //发送消息,校验结果并做相应的动作
                    mHandler.sendMessage(message);
                }
            });
        } else if (BindDeviceActivity.INTENT_SCAN_DEVICE_FOR_BIND.equals(fromActivity)) {
            //返回绑定界面
            Intent intent = new Intent();
            //传递参数
            intent.putExtra(BindDeviceActivity.EXTRAS_SELECTED_DEVICE_NAME, mSelectedBluetoothDevice.getName());
            intent.putExtra(BindDeviceActivity.EXTRAS_SELECTED_DEVICE_ADDRESS, mSelectedBluetoothDevice.getAddress());
            setResult(RESULT_OK, intent);
            //结束活动
            finish();
        }
    }

    //初始化蓝牙适配器
    private void initBleAdapter() {
        //检查是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "你的设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        //初始化蓝牙适配器，获取设备自身的蓝牙适配器 https://developer.android.com/guide/topics/connectivity/bluetooth-le#setupzzzzzzz
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //检查设备是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "你的设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //初始化定位组件
    private void initLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            Toast.makeText(this, "你的设备不支持定位", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}