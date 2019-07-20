package com.example.pc.newble;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
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
import com.example.pc.newble.Activities.BLEActivity;
import com.example.pc.newble.SQLite.MyDBHandler;
import com.example.pc.newble.SQLite.Products;
import com.example.pc.newble.TheUtils.DateUtil;
import com.example.pc.newble.TheUtils.FileUtils;
import com.example.pc.newble.TheUtils.HexUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class BLEReadService extends Service {


    private final String CHANNEL_ID = "TEST_SERVICE_ID";
    private final String CHANNEL_NAME = "渠道一";

    private final String contentSub = "小标题";
    private final String contentTitle = "BLE Service";
    private final String contentText = "正持续记录您的电位……";
    Notification notification;
    Notification.Builder builder;


    private static final String TAG = "MyService";


    // =============================================================================
    /*
    private MyDBHandler dbHandler;

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
    */
    // ===================================================================================


    private DownloadBinder mBinder = new DownloadBinder();
    class DownloadBinder extends Binder {
        public void startDownload() {
            Log.d(TAG, "startDownload: 开始下载");
        }
        public int getProgress(){
            Log.d(TAG, "getProgress: 查看进度");
            return 0;
        }
    }

    public BLEReadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
        //    throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 哈哈哈哈");
        // 前台服务
        /*
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("dsfaafdsfads")
                .setContentText("asfsadfdsaffsadfds")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 哈哈哈");
        //   return super.onStartCommand(intent, flags, startId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            chan.enableLights(true);
            chan.setLightColor(Color.RED);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            builder = new Notification.Builder(this, CHANNEL_ID);
            notification = builder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(contentText)
                    .setSubText(contentSub)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(contentTitle)
                    .build();
        }
        startForeground(1, notification);










        return START_STICKY;

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy: 哈哈哈哈哈哈");
    }




}
