package com.example.pc.lbs.service;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * Author: Hadeslock
 * Created on 2022/4/29 16:15
 * Email: hadeslock@126.com
 * Desc: 低功耗蓝牙服务类，参考开发者指南和官方示例
 * 开发者指南：<a href="https://developer.android.google.cn/guide/topics/connectivity/bluetooth-le">蓝牙低功耗概览</a>
 * 官方示例：<a href="https://github.com/android/connectivity-samples/tree/main/BluetoothLeGatt/">蓝牙连接服务示例</a>
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    //本地的binder，用于对外提供本服务的引用
    private final IBinder mBinder = new LocalBinder();

    private BluetoothManager mBluetoothManager; //蓝牙设备管理器
    private BluetoothAdapter mBluetoothAdapter; //蓝牙设备适配器
    private BluetoothGatt mBluetoothGatt; //蓝牙设备配置文件

    private String mBluetoothDeviceAddress; //蓝牙设备地址
    private int mConnectionState = STATE_DISCONNECTED; //连接的状态

    //蓝牙状态码
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //广播识别码
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    //传输数据key
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    //电位数据的UUID
    public static String POTENTIAL_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_POTENTIAL_MEASUREMENT =
            UUID.fromString(POTENTIAL_MEASUREMENT);

    // 为应用关心的 GATT 事件实现回调方法。例如，连接更改和发现的服务
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        // 回调指示 GATT 客户端何时与远程 GATT 服务器断开连接
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            //判断新的连接状态
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //设置连接状态
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //广播连接状态
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // 连接成功后尝试发现服务
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //设置连接状态
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                //广播连接状态
                broadcastUpdate(intentAction);
            }
        }

        //当远程设备的远程服务、特征和描述符列表已更新时调用回调，即已发现新服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        //回调报告特征读取操作的结果
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    /*
     * 向外广播识别码
     * @author hadeslock
     * @date 2022/4/17 14:39
     * @param action 广播识别码
     * @return
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //向外广播识别码，并对特征进行一些处理
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // 对接收到的数据进行一定的处理
        if (UUID_POTENTIAL_MEASUREMENT.equals(characteristic.getUuid())) {
            //接收到电位数据
            final byte[] data = characteristic.getValue();// 接收到的二进制数据
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data));
            }
        } else {
            // 对于所有其他配置文件，以 HEX 格式写入数据。
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    //将通信通道返回给服务。如果客户端无法绑定到服务，则可能返回 null。返回的 IBinder 通常用于已使用aidl 描述的复杂接口。
    // 请注意，与其他应用程序组件不同，对此处返回的 IBinder 接口的调用可能不会发生在进程的主线程上。
    //这里简单返回自定义的binder的引用
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //当所有客户端与服务发布的特定接口断开连接时调用。默认实现不执行任何操作并返回 false。
    @Override
    public boolean onUnbind(Intent intent) {
        // 使用给定设备后，您应该确保调用 BluetoothGatt.close() 以便正确清理资源。
        // 在此特定示例中，当 UI 与服务断开连接时调用 close()。
        close();
        return super.onUnbind(intent);
    }

    //自定义的连接器，这里对外提供了服务类内部的一些的引用
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    /*
     * 使用给定的 BLE 设备后，应用程序必须调用此方法以确保正确释放资源。
     * @author hadeslock
     * @date 2022/4/17 14:34
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /*
     * 初始化对本地蓝牙适配器的引用
     * @author hadeslock
     * @date 2022/4/17 15:48
     * @param
     * @return boolean true-初始化成功 false-初始化失败
     */
    public boolean initialize() {
        // 对于 API 级别 18 及以上，通过 BluetoothManager 获取对 BluetoothAdapter 的引用。
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /*
     * 连接到蓝牙 LE 设备上托管的 GATT 服务器
     * @author hadeslock
     * @date 2022/4/17 15:52
     * @param address 目标设备的设备地址
     * @return boolean true-连接成功 false-连接失败
     */
    public boolean connect(final String address) {
        //蓝牙适配器未初始化 或 传入空地址
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // 以前连接的设备。尝试重新连接。
        if (address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        //之前没有连接到这个设备，获取要连接的设备信息
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // 蓝牙连接到设备设备的GATT服务器，https://developer.android.com/guide/topics/connectivity/bluetooth-le#connect
        // 直接连接到设备，所以将 autoConnect 参数设置为 false。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断了一下手机系统，6.0及以上连接设备的方法
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback, TRANSPORT_LE);
        } else {
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        }
        Log.d(TAG, "Trying to create a new connection.");
        //设置本地参数
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /*
     * 断开现有连接或取消挂起的连接
     * @author hadeslock
     * @date 2022/4/17 15:54
     * @param
     * @return void
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /*
     * 请求读取给定的 BluetoothGattCharacteristic
     * @param characteristic 要读取的特征。
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /*
     * 启用或禁用给定特征的通知。
     * @param characteristic 采取行动的特征。
     * @param enabled 如果为 true，则启用通知。否则为 false。
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        //蓝牙适配器未初始化
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /*
     * 检索连接设备上支持的 GATT 服务列表。这应该仅在 BluetoothGatt#discoverServices() 成功完成后调用
     * @author hadeslock
     * @date 2022/4/18 13:51
     * @param
     * @return List<BluetoothGattService> GATT服务列表
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
}