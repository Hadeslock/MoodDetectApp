package com.example.pc.lbs.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.GsonUtil;
import com.example.pc.lbs.TheUtils.HttpUtil;
import com.example.pc.lbs.pojo.Device;
import com.example.pc.lbs.pojo.RespBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class BindDeviceActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = BindDeviceActivity.class.getSimpleName();

    //请求扫描设备进行绑定
    public static final String INTENT_SCAN_DEVICE_FOR_BIND = TAG + ".SCAN_DEVICE_FOR_BIND";
    private static final int REQUEST_SCAN_DEVICE = 1;

    //活动间传递数据
    public static final String EXTRAS_SELECTED_DEVICE_NAME = "SelectedDeviceName"; //选择的设备名称key
    public static final String EXTRAS_SELECTED_DEVICE_ADDRESS = "SelectedDeviceAddress"; //选择的设备地址key

    private String deviceName; //选择的设备名称
    private String deviceAddress; //选择的设备地址

    //界面组件
    private Button selectBtn;
    private Button bindBtn;
    private TextView selectedDeviceName;

    //消息码
    private static final int NETWORK_FAILURE = 1; //网络错误
    private static final int BIND_DEVICE_FINISH = 2; //绑定设备结束

    //activity创建的钩子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);

        initView();
    }

    //初始化界面
    private void initView() {
        //初始化组件引用
        selectBtn = findViewById(R.id.bind_device_select_btn);
        bindBtn = findViewById(R.id.bind_device_bind_btn);
        selectedDeviceName = findViewById(R.id.bind_device_selected_device_name);
        //设置组件点击回调
        selectBtn.setOnClickListener(this);
        bindBtn.setOnClickListener(this);
    }

    //点击事件回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.bind_device_select_btn == id) {
            //点击选择设备按钮 跳转到设备扫描界面
            Intent intent = new Intent(this, ScanDeviceActivity.class);
            intent.putExtra("fromActivity", INTENT_SCAN_DEVICE_FOR_BIND);
            startActivityForResult(intent, REQUEST_SCAN_DEVICE);
        } else if (R.id.bind_device_bind_btn == id) {
            //点击绑定设备按钮
            bindBtn.setEnabled(false); //使按钮失效，防止重复发送
            //生成请求参数
            Device device = new Device(deviceAddress, deviceName);
            RequestBody requestBody = GsonUtil.generateRequestBody(device, "json");
            String addDeviceUrl = BuildConfig.baseUrl + "device/addDevice";
            HttpUtil.postRequestWithJson(addDeviceUrl, requestBody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Message message = new Message();
                    message.what = NETWORK_FAILURE;
                    mHandler.sendMessage(message);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //解析返回的响应
                    RespBean respBean = RespBean.parseResponse(response);
                    //分情况处理
                    Message message = new Message();
                    message.what = BIND_DEVICE_FINISH;
                    //存储返回的消息
                    Bundle bundle = new Bundle();
                    bundle.putString("responseMsg", respBean.getMessage());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_SCAN_DEVICE == requestCode && RESULT_OK == resultCode) {
            //选择设备成功
            if (data != null) {
                deviceName = data.getStringExtra(EXTRAS_SELECTED_DEVICE_NAME);
                deviceAddress = data.getStringExtra(EXTRAS_SELECTED_DEVICE_ADDRESS);
            }
            Log.i(TAG, "接收到被点击的设备数据: deviceName = " + deviceName
                    + "\ndeviceAddress = " + deviceAddress);
            selectedDeviceName.setText(deviceName);
        }
    }

    //消息回调
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (NETWORK_FAILURE == what) {//网络错误
                Toast.makeText(BindDeviceActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                bindBtn.setEnabled(true);
                return true;
            } else if (BIND_DEVICE_FINISH == what) { //绑定请求结束
                //提示绑定结果
                Toast.makeText(BindDeviceActivity.this, msg.getData().getString("responseMsg"),
                        Toast.LENGTH_SHORT).show();
                //使能按钮
                bindBtn.setEnabled(true);
                return true;
            }
            return false;
        }
    });
}