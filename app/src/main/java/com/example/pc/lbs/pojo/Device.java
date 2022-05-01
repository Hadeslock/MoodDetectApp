package com.example.pc.lbs.pojo;

import com.example.pc.lbs.utils.GsonUtil;
import lombok.Data;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/18 16:41
 * Email: hadeslock@126.com
 * Desc: 与后端交互的设备实体类
 */
@Data
public class Device {
    private Integer id;
    private String device_id;
    private String device_name;

    public Device(String deviceAddress, String deviceName) {
        this.device_id = deviceAddress;
        this.device_name = deviceName;
    }

    /*
     * 从response中解析出设备列表
     * @author hadeslock
     * @date 2022/4/18 19:45
     * @param response 要解析的响应
     * @return List<Device> 解析出的设备列表
     */
    public static List<Device> parseDeviceListFromResponse(Response response) throws IOException {
        List<Device> res = null;
        if (response != null && response.body() != null) {
            String responseBody = response.body().string();
            res = GsonUtil.jsonToList(responseBody, Device.class);
        }
        return res;
    }
}
