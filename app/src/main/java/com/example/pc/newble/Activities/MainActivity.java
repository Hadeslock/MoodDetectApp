package com.example.pc.newble.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pc.newble.R;
import com.example.pc.newble.SQLite.MyDBHandler;
import com.example.pc.newble.TheUtils.DateUtil;
import com.example.pc.newble.TheUtils.FileUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
/**
 * 为了寻找一些函数方便，LocationActivity，BaseStripAdapter,Utils,StripItem,StripListView之后可以删
 * LocationApplication，locationservice，不删也不需要做改动
 *
 *
 */

public class MainActivity extends AppCompatActivity {

    // 显示图表时每两个点之间点间隔时间。如果设的太小，可能导致载入较慢
    public static final int TIME_INTERVAL = 60;

    private final int SDK_PERMISSION_REQUEST = 127;
    private ListView FunctionList;
    private String permissionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 创建 DataList.txt 文件
        FileUtils.makeFilePath(FileUtils.getSDCardPath() + "/bletest/",
                "DataList.txt");
        FunctionList = (ListView) findViewById(R.id.functionList);
        FunctionList
                .setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getData()));//listview 使用

        // after andrioid m,must request Permiision on runtime
        getPersimmions();


    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    /**
     * 处理 MainActivity 的 ListView 跳转的函数。
     * */
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        FunctionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                Class<?> TargetClass = null;
                switch (arg2) {
                    case 0:  // 跳转蓝牙连接
                        TargetClass = BLEActivity.class;
                        break;

                    case 1:   // 跳转查看记录
                        TargetClass = ChooseHistActivity.class;
                        break;
                    /*case 2:    // 自定义回调示例  （有时无效）
                        TargetClass = LocationActivity.class;
                        break;*/
                    case 2:    // 连续定位示例
                        TargetClass = Location2Activity.class;
                        break;
                    case 3:    // test activity
                        TargetClass = TestActivity.class;
                        break;
                    case 5:

                    default:
                        break;
                }
                if (TargetClass != null) {
                    Intent intent = new Intent(MainActivity.this, TargetClass);
                    intent.putExtra("from", 0);
                    startActivity(intent);
                }
            }
        });
    }

    private List<String> getData() {   //条目

        List<String> data = new ArrayList<String>();
        data.add("跳转蓝牙连接");
        data.add("跳转查看记录");
     //   data.add("自定义回调示例");
        data.add("连续定位示例");
        data.add("从csv文件中读取");
     //   data.add("室内定位功能");
     //   data.add("判断移动热点");
     //   data.add("android 8.0后台定位示例");
     //   data.add("常见问题说明");

        return data;
    }
}
