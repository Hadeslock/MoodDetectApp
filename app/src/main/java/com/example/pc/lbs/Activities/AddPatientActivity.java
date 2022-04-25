package com.example.pc.lbs.Activities;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.GsonUtil;
import com.example.pc.lbs.TheUtils.HttpUtil;
import com.example.pc.lbs.permission.PermissionHelper;
import com.example.pc.lbs.permission.PermissionInterface;
import com.example.pc.lbs.pojo.Patient;
import com.example.pc.lbs.pojo.RespBean;
import okhttp3.Call;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * Author: Hadeslock
 * Created on 2022/4/13 13:41
 * Email: hadeslock@126.com
 * Desc: 这个activity用于添加病人
 */

public class AddPatientActivity extends AppCompatActivity
        implements View.OnClickListener, PermissionInterface {

    private static final String TAG = "AddPatientActivity";

    //消息代码
    private static final int MSG_ADD_PATIENT_SUCCESS = 1;
    private static final int MSG_ADD_PATIENT_FAILURE = 2;
    private static final int MSG_NETWORK_FAILURE = 3;
    private static final int RES_ADD_PATIENT_SUCCESS = 1; //成功添加病人的响应码

    //界面组件引用
    private EditText addPatientName;
    private EditText addPatientAge;
    private RadioGroup addPatientGender;
    private TextView addPatientPosition;
    private EditText addPatientIdentity;
    private Button addPatientAddBtn;

    private String selectGender; //当前选择的性别 男-0 女-1
    private String curPosition = "未获取地址"; //当前位置

    //定位相关
    private LocationClient mLocationClient; //定位服务客户端

    //动态权限
    private PermissionHelper mPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        //获取权限
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    /*
     * 初始化界面引用
     * @author Hadeslock
     * @time 2022/4/14 14:17
     */
    private void initView() {
        //初始化界面组件引用
        addPatientName = findViewById(R.id.add_patient_name);
        addPatientAge = findViewById(R.id.add_patient_age);
        addPatientGender = findViewById(R.id.add_patient_gender);
        addPatientPosition = findViewById(R.id.add_patient_position);
        addPatientIdentity = findViewById(R.id.add_patient_identity);
        addPatientAddBtn = findViewById(R.id.add_patient_addBtn);

        //设置点击事件回调
        addPatientAddBtn.setOnClickListener(this);
        addPatientGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checked = findViewById(checkedId);
                selectGender = checked.getText().toString();
            }
        });
    }

    /*
     * 初始化定位服务
     * @author Hadeslock
     * @time 2022/4/13 20:36
     */
    private void initLocationService() {
        //初始化客户端
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        //定位选项
        LocationClientOption option = new LocationClientOption();
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(5000);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，是否需要位置描述信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的位置信息，此处必须为true
        option.setIsNeedLocationDescribe(true);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocationClient.setLocOption(option);
        //启动服务
        mLocationClient.start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.add_patient_addBtn == id) {//添加病人按钮
            addPatientAddBtn.setEnabled(false);
            //解析参数
            String name = addPatientName.getText().toString();
            int age = Integer.parseInt(addPatientAge.getText().toString());
            int gender = "男".equals(selectGender) ? 0 : 1;
            String identity = addPatientIdentity.getText().toString();
            Patient patient = new Patient(name, age, gender, curPosition, identity);
            //生成请求参数
            RequestBody requestBody = GsonUtil.generateRequestBody(patient, "json");
            String addPatientUrl = BuildConfig.baseUrl + "patient/addPatient";
            HttpUtil.postRequestWithJson(addPatientUrl, requestBody, new okhttp3.Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //解析返回的响应
                    RespBean respBean = RespBean.parseResponse(response);
                    //分情况处理
                    Message message = new Message();
                    long code = respBean.getCode();
                    if (200 == code) {
                        message.what = MSG_ADD_PATIENT_SUCCESS;
                    } else {
                        message.what = MSG_ADD_PATIENT_FAILURE;
                    }
                    mHandler.sendMessage(message);
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Message message = new Message();
                    message.what = MSG_NETWORK_FAILURE;
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    //地图服务监听，重写回调以更新ui
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //设置地址信息
            curPosition = bdLocation.getAddrStr();
            addPatientPosition.setText(curPosition);
            Log.d(TAG, "onReceiveLocation: 获取到地址" + curPosition
                    + "\n代码：" + bdLocation.getLocType());
        }
    }

    //消息回调
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (MSG_ADD_PATIENT_SUCCESS == what) {//添加成功
                Toast.makeText(AddPatientActivity.this, "添加病人成功", Toast.LENGTH_SHORT).show();
                setResult(RES_ADD_PATIENT_SUCCESS); //设置响应号
                finish();
            } else if (MSG_ADD_PATIENT_FAILURE == what) { //添加失败
                Toast.makeText(AddPatientActivity.this, "添加病人失败", Toast.LENGTH_SHORT).show();
                addPatientAddBtn.setEnabled(true);
            } else if (MSG_NETWORK_FAILURE == what) { //网络错误
                Toast.makeText(AddPatientActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                addPatientAddBtn.setEnabled(true);
            }
            return true;
        }
    });

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
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        //所有权限都已满足，可以进行界面操作
        initView(); //初始化界面引用
        initLocationService(); //初始化定位服务
    }

    @Override
    public void requestPermissionsFail() {
        //权限未满足
        Toast.makeText(this, "未满足所有权限，无法使用", Toast.LENGTH_SHORT).show();
        finish();
    }
    // --------------------- 动态权限校验回调结束 ---------------------
}