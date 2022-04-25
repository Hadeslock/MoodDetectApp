package com.example.pc.lbs.Activities;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.HttpUtil;
import com.example.pc.lbs.pojo.Patient;
import okhttp3.Call;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/21 14:19
 * Email: hadeslock@126.com
 * Desc: 这个activity是用于选择病人的
 */
public class SelectPatientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SelectPatientActivity.class.getSimpleName();

    //消息码
    private static final int MSG_SET_PATIENT_ADAPTER = 0; //设置病人列表数据源消息码
    private static final int MSG_NETWORK_FAILURE = 1;
    private static final int ACTION_ADD_PATIENT = 1; //跳转添加病人的请求码

    //上一个活动传递过来的数据
    private BluetoothDevice extrasDevice;

    //界面组件
    private Spinner patientListSpinner; //病人下拉列表
    private Button addPatientBtn; //添加病人按钮
    private Button ensureSelectPatientBtn; //确认选择病人按钮
    //数据
    private List<Patient> patientList; //病人列表数据
    private Patient selectedPatient; //下拉列表选择的病人

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_patient);

        //获取上一个活动传过来的数据
        Intent intent = getIntent();
        extrasDevice = intent.getParcelableExtra(DeviceMeasureActivity.EXTRAS_SELECTED_DEVICE);

        initView(); //初始化界面引用
        initPatientList(); //初始化病人列表
    }

    //点击回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.add_patient_button == id) {
            //跳转到添加病人页面并处理返回结果
            Intent intent = new Intent(SelectPatientActivity.this, AddPatientActivity.class);
            startActivityForResult(intent, ACTION_ADD_PATIENT);
        } else if (R.id.select_patient_ensure_btn == id) {
            //确认选择病人，跳转到测量界面
            Intent intent = new Intent(SelectPatientActivity.this, DeviceMeasureActivity.class);
            //传递参数
            intent.putExtra(DeviceMeasureActivity.EXTRAS_SELECTED_DEVICE, extrasDevice);
            intent.putExtra(DeviceMeasureActivity.EXTRAS_SELECTED_PATIENT_ID, selectedPatient.getId());
            intent.putExtra(DeviceMeasureActivity.EXTRAS_SELECTED_PATIENT_NAME, selectedPatient.getName());
            startActivity(intent);
            //结束活动
            finish();
        }
    }

    /*
     * 初始化界面引用
     * @author Hadeslock
     * @time 2022/4/14 15:10
     */
    private void initView() {
        //初始化组件引用
        patientListSpinner = findViewById(R.id.select_patient_spinner);
        addPatientBtn = findViewById(R.id.add_patient_button);
        ensureSelectPatientBtn = findViewById(R.id.select_patient_ensure_btn);

        //添加点击回调
        addPatientBtn.setOnClickListener(this);
        ensureSelectPatientBtn.setOnClickListener(this);
        patientListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPatient = patientList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*
     * 初始化病人列表数据
     * @author Hadeslock
     * @time 2022/4/14 15:12
     */
    private void initPatientList() {
        //初始化病人列表
        String getPatientsUrl = BuildConfig.baseUrl + "patient/allPatients";
        HttpUtil.getRequest(getPatientsUrl, new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //解析响应，恢复病人列表
                if (response.body() != null) {
                    //设置病人列表
                    patientList = Patient.parsePatients(response);
                    //发送消息，设置数据源
                    Message message = new Message();
                    message.what = MSG_SET_PATIENT_ADAPTER;
                    mHandler.sendMessage(message);
                } else {
                    Log.e(TAG, "onResponse: 未获取到病人数据");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message message = new Message();
                message.what = MSG_NETWORK_FAILURE;
                mHandler.sendMessage(message);
            }
        });
    }

    //消息回调
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_SET_PATIENT_ADAPTER) {//设置下拉列表数据源
                List<String> patientNames = Patient.getPatientNameList(patientList);
                ArrayAdapter<String> patientArrayAdapter = new ArrayAdapter<>(
                        SelectPatientActivity.this, android.R.layout.simple_list_item_1,
                        patientNames);
                patientListSpinner.setAdapter(patientArrayAdapter);
            } else if (what == MSG_NETWORK_FAILURE) {
                Toast.makeText(SelectPatientActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    });
}