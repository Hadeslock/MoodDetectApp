package com.example.pc.lbs.module;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.util.Log;
import com.blankj.utilcode.util.StringUtils;
import com.example.pc.lbs.MyApplication;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Author: Hadeslock
 * Created on 2022/4/12 19:09
 * Email: hadeslock@126.com
 * Desc: okhttp的拦截器类 <a href="https://blog.csdn.net/muyi_amen/article/details/58586823">https://blog.csdn.net/muyi_amen/article/details/58586823</a>
 */
public class OkhttpInterceptor {

    private static final String TAG = "OkhttpInterceptor";

    private static String JSESSIONID; //全局http连接设置session

    /*
     * app层面的响应拦截器
     * @author Hadeslock
     * @time 2022/4/12 19:10
     */
    public static Interceptor appInterceptor = new Interceptor() {
        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            //---------请求之前------------
            Log.d(TAG, "app interceptor:begin");
            //添加session id
            if (!StringUtils.isEmpty(JSESSIONID)) {
                request = request.newBuilder().header("Cookie", "JSESSIONID=" + JSESSIONID).build();
            }
            //添加token
            SharedPreferences data = MyApplication.getContext()
                    .getSharedPreferences("data", Context.MODE_PRIVATE);
            String token = data.getString("tokenStr", null);
            if (!StringUtils.isEmpty(token)) {
                request = request.newBuilder().header("Authorization", token).build();
            }
            //---------发起请求------------
            Response response = chain.proceed(request);
            //---------请求之后------------
            Headers headers = response.headers();
            //保存sessionid
            String cookies = headers.get("Set-Cookie");
            if (!StringUtils.isEmpty(cookies)) {
                for (String cookie : cookies.split(";")) {
                    String[] res = cookie.split("=");
                    if ("JSESSIONID".equals(res[0])) {
                        Log.d(TAG, "intercept: 获取到sessionid -- " + res[1]);
                        JSESSIONID = res[1];
                        break;
                    }
                }
            }
            Log.d(TAG, "app interceptor:end");
            return response;
        }
    };
}
