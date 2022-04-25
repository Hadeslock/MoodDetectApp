package com.example.pc.lbs.pojo;

import com.example.pc.lbs.TheUtils.GsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.Response;

import java.io.IOException;

/**
 * Author: Hadeslock
 * Created on 2022/4/12 18:50
 * Email: hadeslock@126.com
 * Desc: 后端返回结果的实体类
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean {

    private long code;
    private String message;
    private Object obj;

    /*
     * 从response中解析respbean
     * @author Hadeslock
     * @time 2022/4/13 13:58
     */
    public static RespBean parseResponse(Response response) throws IOException {
        if (response == null || response.body() == null) {
            throw new IOException("ResponseBody is null");
        } else {
            String responseBody = response.body().string();
            return GsonUtil.gson.fromJson(responseBody, RespBean.class);
        }
    }
}
