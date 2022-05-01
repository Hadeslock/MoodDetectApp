package com.example.pc.lbs.pojo;

import com.example.pc.lbs.utils.GsonUtil;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/13 14:41
 * Email: hadeslock@126.com
 * Desc: 病人对象
 */

@Data
public class Patient {
    private Integer id; //病人id
    private String name; //病人姓名
    private Integer age; //病人年龄
    private Integer gender; //病人性别
    private String position; //病人地理位置
    private String identity; //病人身份证号

    public Patient(String name, Integer age, Integer gender, String position, String identity) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.position = position;
        this.identity = identity;
    }

    /*
     * 从返回结果中解析病人列表
     * @author Hadeslock
     * @time 2022/4/13 15:49
     */
    public static List<Patient> parsePatients(Response response) throws IOException {
        if (response == null || response.body() == null) {
            throw new IOException("Response is null");
        }
        // https://blog.csdn.net/londa/article/details/116097938
        Type type = new TypeToken<List<Patient>>() {
        }.getType();
        String responseBody = response.body().string();
        return GsonUtil.gson.fromJson(responseBody, type);
    }

    /*
     * 从病人列表中获取病人名称的列表
     * @author Hadeslock
     * @time 2022/4/13 19:14
     */
    public static List<String> getPatientNameList(List<Patient> patientList) {
        List<String> list = new ArrayList<>();
        for (Patient patient : patientList) {
            list.add(patient.getName());
        }
        return list;
    }
}
