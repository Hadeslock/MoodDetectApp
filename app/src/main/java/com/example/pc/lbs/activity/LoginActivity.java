package com.example.pc.lbs.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.utils.DateUtil;
import com.example.pc.lbs.utils.GsonUtil;
import com.example.pc.lbs.utils.HttpUtil;
import com.example.pc.lbs.pojo.LoginParam;
import com.example.pc.lbs.pojo.RespBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

/**
 * Author: Hadeslock
 * Created on 2022/4/11 18:56
 * Email: hadeslock@126.com
 * Desc: 登录活动
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    //界面上的组件
    Button btnLogin;//登录按钮
    Button btnRegister;//注册按钮
    TextView lgUsername; //用户名输入框
    TextView lgPassword; //密码输入框
    TextView lgCaptcha; //验证码输入框
    ImageView lgCaptchaView; //验证码图片

    private boolean isLogin = false; //登录状态

    private final int LOGIN_SUCCESS = 1;//注册成功的消息
    private final int LOGIN_FALUIRE = 2;//注册成功的消息
    private final int NETWORK_FALUIRE = 3; // 网络错误信息
    private final int UNRESOLVED_ERROR = 4; // 未知错误

    //创建活动时的钩子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化界面组件的引用
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        lgUsername = findViewById(R.id.userName);
        lgPassword = findViewById(R.id.password);
        lgCaptcha = findViewById(R.id.loginCaptcha);
        lgCaptchaView = findViewById(R.id.loginCaptchaImageView);

        //获取验证码图片
        final String captchaUrl = BuildConfig.baseUrl + "captcha" + "?time=" + DateUtil.getNowDateTime();
        Glide.with(this).load(captchaUrl).into(lgCaptchaView);

        //设置组件的回调
        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        lgCaptchaView.setOnClickListener(this);

        //从SharedPreference中读取登录状态
        SharedPreferences data = getSharedPreferences("data", MODE_PRIVATE);
        isLogin = data.getBoolean("isLogin", false);
        //检查是否登录，若已登录，前往功能主页
        if (this.isLogin) {
            Log.d(TAG, "已登录，前往主功能页面");
            navigateTo(MainActivity.class);
            finish();//关闭登录页
        }
    }

    /*
     * 尝试登录
     * @author Hadeslock
     * @time 2022/4/11 19:21
     */
    private void checkLogin() {
        //发送登录请求，尝试登录
        //获取请求参数
        String username = lgUsername.getText().toString();
        String password = lgPassword.getText().toString();
        String captcha = lgCaptcha.getText().toString();
        //检查参数是否都写了
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(captcha)) {
            //有部分参数没写
            Toast.makeText(this, "请输入全部参数", Toast.LENGTH_SHORT).show();
            return;
        }
        LoginParam loginParam = new LoginParam(username, password, captcha);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String loginUrl = BuildConfig.baseUrl + "login";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");//请求设置成json
        Log.d(TAG, "checkLogin: the login param is\n" + gson.toJson(loginParam));
        RequestBody requestBody = RequestBody.create(gson.toJson(loginParam), JSON);
        //异步发送请求
        HttpUtil.postRequestWithJson(loginUrl, requestBody, new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //解析返回的响应
                RespBean respBean = RespBean.parseResponse(response);
                //分情况处理
                Message message = new Message();
                long code = respBean.getCode();
                if (200 == code) {
                    //登录成功
                    isLogin = true;
                    Log.d(TAG, "checkLogin: 登录成功 -- " + respBean.getMessage());
                    message.what = LOGIN_SUCCESS;
                    //解析token
                    Map map = GsonUtil.gson.fromJson(respBean.getObj().toString(), Map.class);
                    String token = (String) map.get("tokenHead") + map.get("token");
                    Log.d(TAG, "onResponse: 获取到token：" + token);
                    //保存登录状态和token到SharedPreference
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString("tokenStr", token);
                    editor.putBoolean("isLogin", isLogin);
                    editor.apply();
                } else if (500 == code) {
                    //登录失败
                    message.what = LOGIN_FALUIRE;
                    Log.e(TAG, "onResponse: 登录失败 -- " + respBean.getMessage());
                    Bundle data = new Bundle();
                    data.putString("errorInfo", respBean.getMessage());
                    message.setData(data);
                } else {
                    //未知错误
                    message.what = UNRESOLVED_ERROR;
                }
                //调用消息回调
                mHandler.sendMessage(message);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //网络错误
                Message message = new Message();
                message.what = NETWORK_FALUIRE;
                mHandler.sendMessage(message);
            }
        });
    }

    //点击事件回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.btn_login == id) {
            //登录按钮点击
            checkLogin();
            btnLogin.setEnabled(false);
        } else if (R.id.btn_register == id) {
            //注册按钮点击
            navigateTo(RegisterActivity.class);
        } else if (R.id.loginCaptchaImageView == id) {
            //点击验证码图片更新
            final String captchaUrl = BuildConfig.baseUrl + "captcha" + "?time=" + DateUtil.getNowDateTime();
            Glide.with(this).load(captchaUrl).into(lgCaptchaView);
        }
    }


    //消息回调，用于处理一些异步线程中无法处理的事情，比如ui操作，发送toast
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    //登录成功
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    navigateTo(MainActivity.class);
                    finish();
                    break;
                case LOGIN_FALUIRE:
                    //登陆失败
                    String errorInfo = msg.getData().getString("errorInfo");
                    Toast.makeText(LoginActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    break;
                case UNRESOLVED_ERROR:
                    //未知错误
                    Toast.makeText(LoginActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    break;
                case NETWORK_FALUIRE:
                    //网络错误
                    Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    /*
     * 自定义的导航到下一activity的函数
     * @author Hadeslock
     * @time 2022/4/11 19:33
     */
    private void navigateTo(Class<?> clss) {
        Intent intent = new Intent(this, clss);
        startActivity(intent);
    }
}