package com.example.pc.lbs.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.TheUtils.GsonUtil;
import com.example.pc.lbs.TheUtils.HttpUtil;
import com.example.pc.lbs.pojo.RespBean;
import com.example.pc.lbs.pojo.User;
import okhttp3.Call;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import static android.text.TextUtils.isEmpty;

/**
 * Author: Hadeslock
 * Created on 2022/4/11 18:56
 * Email: hadeslock@126.com
 * Desc: 注册活动
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private final int REGISTER_SUCCESS = 1;//注册成功的消息
    private final int REGISTER_FAILURE = 2;//注册成功的消息
    private final int NETWORK_FAILURE = 3; // 网络错误信息

    //界面组件
    private Button btnRegister;
    private EditText registerUsername;
    private EditText registerPassword;
    private EditText registerPhone;
    private EditText registerEmail;


    //创建时的钩子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //初始化界面组件的引用
        //界面上的组件
        btnRegister = findViewById(R.id.btn_immRegister);
        registerUsername = findViewById(R.id.registerUserName);
        registerPassword = findViewById(R.id.registerPassword);
        registerPhone = findViewById(R.id.registerPhone);
        registerEmail = findViewById(R.id.registerEmail);
        //设置组件的回调
        btnRegister.setOnClickListener(this);
    }

    //点击事件回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "onClick: id");
        if (R.id.btn_immRegister == id) {//立即注册按钮点击
            //获取输入参数
            String username = registerUsername.getText().toString();
            String password = registerPassword.getText().toString();
            String phone = registerPhone.getText().toString();
            String email = registerEmail.getText().toString();
            //检查参数是否都写了
            if (isEmpty(username) || isEmpty(password) || isEmpty(phone) || isEmpty(email)) {
                //有部分参数没写
                Toast.makeText(this, "请输入全部参数", Toast.LENGTH_SHORT).show();
                return;
            }
            //禁用按钮
            btnRegister.setEnabled(false);
            //发起注册的http请求 https://square.github.io/okhttp/#post-to-a-server
            //获取请求参数
            User user = new User(username, password, email, phone);
            String registerUrl = BuildConfig.baseUrl + "register";
            RequestBody requestBody = GsonUtil.generateRequestBody(user, "json");
            //异步发送请求
            HttpUtil.postRequestWithJson(registerUrl, requestBody, new okhttp3.Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    //解析返回的响应
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    RespBean respBean = GsonUtil.gson.fromJson(responseBody, RespBean.class);
                    //分情况处理
                    Message message = new Message();
                    long code = respBean.getCode();
                    if (200 == code) {
                        message.what = REGISTER_SUCCESS;
                    } else {
                        message.what = REGISTER_FAILURE;
                    }
                    mHandler.sendMessage(message);
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Message message = new Message();
                    message.what = NETWORK_FAILURE;
                    mHandler.sendMessage(message);
                }
            });
        }
    }

    //----------- 消息回调，用于处理一些异步线程中无法处理的事情 ----------------
    //注意这里的写法!!! https://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler

    //自定义的回调
    private static class IncomingHandler extends Handler {
        //静态内部类持有外部类的弱引用
        private final WeakReference<RegisterActivity> mRegisterActivityWeakReference;

        IncomingHandler(RegisterActivity activity) {
            mRegisterActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RegisterActivity activity = mRegisterActivityWeakReference.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private final IncomingHandler mHandler = new IncomingHandler(this);

    /*
     * 为handler准备的回调函数
     * @author Hadeslock
     * @time 2022/4/13 10:38
     */
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case REGISTER_SUCCESS:
                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case REGISTER_FAILURE:
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                btnRegister.setEnabled(true);
                break;
            case NETWORK_FAILURE:
                //网络错误
                Toast.makeText(RegisterActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                btnRegister.setEnabled(true);
                break;
            default:
                break;
        }
    }

}