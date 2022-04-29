package com.example.pc.lbs.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.example.pc.lbs.R;

/**
 * Author: Hadeslock
 * Created on 2022/4/29 16:15
 * Email: hadeslock@126.com
 * Desc: 蓝牙记录的前台提醒服务
 */
public class BLEReadService extends Service {


    private final String CHANNEL_ID = "TEST_SERVICE_ID";
    private final String CHANNEL_NAME = "渠道一";

    private final String contentSub = "小标题";
    private final String contentTitle = "BLE Service";
    private final String contentText = "正持续记录您的电位……";
    Notification notification;
    Notification.Builder builder;


    private static final String TAG = "MyService";


    private DownloadBinder mBinder = new DownloadBinder();

    class DownloadBinder extends Binder {
        public void todo() {
            Log.d(TAG, "todo");
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

        // 前台服务
        /*
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("")
                .setContentText("")
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
    public void onDestroy() {
        super.onDestroy();

    }


}
