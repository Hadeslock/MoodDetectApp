package com.example.pc.lbs.TheUtils;

import com.example.pc.lbs.module.OkhttpInterceptor;
import okhttp3.*;

import java.io.File;

/**
 * Author: Hadeslock
 * Created on 2022/4/12 1:13
 * Email: hadeslock@126.com
 * Desc: http网络请求工具类  参考《第一行代码（第二版）》9.5
 */
public class HttpUtil {

    //网络请求发起服务
    public static final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(OkhttpInterceptor.appInterceptor).build();

    /*
     * 发送http post请求，携带json参数
     * @author Hadeslock
     * @time 2022/4/12 1:13
     */
    public static void postRequestWithJson(String address, RequestBody json, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(address)
                .post(json)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /*
     * 发送http get请求
     * @author Hadeslock
     * @time 2022/4/12 9:39
     */
    public static void getRequest(String address, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /*
     * 上传文件，异步请求
     * @author hadeslock
     * @date 2022/4/20 21:12
     * @param filePath 要上传的文件在手机上的位置
     * @param url 上传到哪个地址
     * @param callback http请求回调
     * @return void
     */
    public static void uploadMultipartFileAsync(String filePath, String url, okhttp3.Callback callback) {
        //构建请求参数
        MultipartBody file = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filePath,
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(filePath)))
                .build();
        //构建请求
        Request request = new Request.Builder()
                .url(url)
                .post(file)
                .build();
        //发送请求
        client.newCall(request).enqueue(callback);
    }
}
