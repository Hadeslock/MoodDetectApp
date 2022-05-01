package com.example.pc.lbs.Activities;

import android.Manifest;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.FileUtils;
import com.example.pc.lbs.permission.PermissionHelper;
import com.example.pc.lbs.permission.PermissionInterface;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        
        //设置底部导航栏控制fragment显示
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        //动态获取权限
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
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
        // 创建 DataList.txt 文件
        FileUtils.makeFilePath(FileUtils.getSDCardPath() + "/bletest/",
                "DataList.txt");
    }

    @Override
    public void requestPermissionsFail() {
        //权限未满足
        Toast.makeText(this, "未满足所有权限，无法使用", Toast.LENGTH_SHORT).show();
        finish();
    }
    // --------------------- 动态权限校验回调结束 ---------------------
}
