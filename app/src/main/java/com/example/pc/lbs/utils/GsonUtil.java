package com.example.pc.lbs.utils;

import com.google.gson.*;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/12 18:54
 * Email: hadeslock@126.com
 * Desc: 自己封装的gson工具类
 */
public class GsonUtil {

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");//请求设置成json

    /*
     * 生成请求体
     * @author Hadeslock
     * @time 2022/4/13 20:57
     */
    public static <T> RequestBody generateRequestBody(T t, String type) {
        RequestBody requestBody = null;
        if ("json".equals(type)) {
            requestBody = RequestBody.create(gson.toJson(t), JSON);
        }
        return requestBody;
    }

    //解析json字符串为List
    public static <T> List<T> jsonToList(String json, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(new Gson().fromJson(elem, cls));
        }
        return list;
    }
}
