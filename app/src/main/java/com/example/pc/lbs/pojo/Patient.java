package com.example.pc.lbs.pojo;

import android.os.Parcel;
import android.os.Parcelable;
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
public class Patient implements Parcelable {
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

    protected Patient(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        if (in.readByte() == 0) {
            age = null;
        } else {
            age = in.readInt();
        }
        if (in.readByte() == 0) {
            gender = null;
        } else {
            gender = in.readInt();
        }
        position = in.readString();
        identity = in.readString();
    }

    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        if (age == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(age);
        }
        if (gender == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(gender);
        }
        dest.writeString(position);
        dest.writeString(identity);
    }
}
