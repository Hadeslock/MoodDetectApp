package com.example.pc.lbs.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.pojo.Patient;
import com.example.pc.lbs.pojo.RespBean;
import com.example.pc.lbs.utils.GsonUtil;
import com.example.pc.lbs.utils.HttpUtil;
import com.lljjcoder.Interface.OnCityItemClickListener;
import com.lljjcoder.bean.CityBean;
import com.lljjcoder.bean.DistrictBean;
import com.lljjcoder.bean.ProvinceBean;
import com.lljjcoder.style.cityjd.JDCityConfig;
import com.lljjcoder.style.cityjd.JDCityPicker;
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
        implements View.OnClickListener {

    private static final String TAG = "AddPatientActivity";

    // region 消息代码
    private static final int MSG_ADD_PATIENT_SUCCESS = 1;
    private static final int MSG_ADD_PATIENT_FAILURE = 2;
    private static final int MSG_NETWORK_FAILURE = 3;
    private static final int RES_ADD_PATIENT_SUCCESS = 1; //成功添加病人的响应码
    // endregion

    // region 界面组件引用
    private EditText addPatientName; //
    private EditText addPatientAge;
    private RadioGroup addPatientGender;
    private TextView addPatientLocation;
    private EditText addPatientAddress;
    private EditText addPatientIdentity;
    private Button addPatientAddBtn;
    // endregion

    //城市选择器
    JDCityPicker cityPicker = new JDCityPicker();
    JDCityConfig jdCityConfig = new JDCityConfig.Builder().build();

    private String selectGender; //当前选择的性别 男-0 女-1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        initView();
        initEvent();
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
        addPatientLocation = findViewById(R.id.add_patient_location);
        addPatientAddress = findViewById(R.id.add_patient_address);
        addPatientIdentity = findViewById(R.id.add_patient_identity);
        addPatientAddBtn = findViewById(R.id.add_patient_addBtn);
    }

    //初始化事件
    private void initEvent() {
        addPatientAddBtn.setOnClickListener(this);
        addPatientGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checked = findViewById(checkedId);
                selectGender = checked.getText().toString();
            }
        });
        addPatientLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jdCityConfig.setShowType(JDCityConfig.ShowType.PRO_CITY_DIS);
                cityPicker.init(AddPatientActivity.this);
                cityPicker.setConfig(jdCityConfig);
                cityPicker.setOnCityItemClickListener(new OnCityItemClickListener() {
                    @Override
                    public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                        String cityResult = province.getName() + city.getName() + district.getName();
                        addPatientLocation.setText(cityResult);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                cityPicker.showCityPicker();
            }
        });
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
            String position = addPatientLocation.getText().toString() + addPatientAddress.getText().toString();
            Patient patient = new Patient(name, age, gender, position, identity);
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
}