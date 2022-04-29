package com.example.pc.lbs.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.FileUtils;
import com.example.pc.lbs.permission.PermissionHelper;
import com.example.pc.lbs.permission.PermissionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/29 16:14
 * Email: hadeslock@126.com
 * Desc: 主功能菜单界面
 */
public class MainActivity extends AppCompatActivity implements PermissionInterface {
    private static final String TAG = MainActivity.class.getSimpleName();

    //请求跳转扫描设备界面，本活动的标识码
    public static final String INTENT_SCAN_DEVICE_FOR_MEASURE = TAG + "INTENT_SCAN_DEVICE_FOR_MEASURE";

    //界面组件
    private ListView FunctionList;

    //动态权限
    private PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建 DataList.txt 文件
        FileUtils.makeFilePath(FileUtils.getSDCardPath() + "/bletest/",
                "DataList.txt");

        //动态获取权限
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    // 初始化界面引用
    private void initView() {
        //初始化组件引用
        FunctionList = findViewById(R.id.functionList);
        //设置组件回调
        FunctionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Class<?> TargetClass = null;
                switch (position) {
                    case 0:  // 跳转蓝牙连接并连接
                        TargetClass = ScanDeviceActivity.class;
                        break;
                    case 1: //绑定设备
                        TargetClass = BindDeviceActivity.class;
                        break;
                    case 2:   // 跳转查看记录
                        TargetClass = TestActivity.class;
                        break;
                    case 3:
                        //查看未上传的记录
                        TargetClass = ViewUnuploadRecordActivity.class;
                        break;
                    case 4: //注销登录
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putBoolean("isLogin", false);
                        editor.apply();
                        TargetClass = LoginActivity.class;
                        break;
                    default:
                        break;
                }
                if (TargetClass != null) {
                    Intent intent = new Intent(MainActivity.this, TargetClass);
                    if (TargetClass == ScanDeviceActivity.class) {
                        intent.putExtra("fromActivity", INTENT_SCAN_DEVICE_FOR_MEASURE);
                    }
                    intent.putExtra("from", 0);//不知道这个数据是哪里用的，不敢删
                    startActivity(intent);
                    //如果是要跳转到登录界面，说明是注销操作，本页面需要被关闭
                    if (TargetClass == LoginActivity.class) {
                        finish();
                    }
                }
            }
        });
        //设置列表的数据源
        List<String> data = new ArrayList<>();
        data.add("连接设备");
        data.add("绑定设备");
        data.add("查看记录并获取结果");
        data.add("查看未上传的记录");
        data.add("注销登录");
        FunctionList.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_expandable_list_item_1, data));
    }

    // --------------------- 动态权限校验回调开始 ---------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        //所有权限都已满足，可以进行界面操作
        initView(); //初始化界面引用
    }

    @Override
    public void requestPermissionsFail() {
        //权限未满足
        Toast.makeText(this, "未满足所有权限，无法使用", Toast.LENGTH_SHORT).show();
        finish();
    }
    // --------------------- 动态权限校验回调结束 ---------------------
}
