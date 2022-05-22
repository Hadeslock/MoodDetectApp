package com.example.pc.lbs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.pojo.Patient;
import com.example.pc.lbs.utils.HttpUtil;
import okhttp3.Call;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/4/21 14:19
 * Email: hadeslock@126.com
 * Desc: 这个activity是用于选择病人的
 */
public class SelectPatientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SelectPatientActivity.class.getSimpleName();

    //消息码
    private static final int MSG_SET_PATIENT_ADAPTER = 0; //设置病人列表数据源消息码
    private static final int MSG_NETWORK_FAILURE = 1;
    private static final int MSG_PATIENT_ITEM_CLICK = 2; //病人列表点击

    private static final int ACTION_ADD_PATIENT = 1; //跳转添加病人的请求码

    //界面组件
    private SearchView searchPatientSV; //病人搜索框
    private RecyclerView patientRV; //病人列表
    private Button addPatientBtn; //添加病人按钮

    //数据
    private List<Patient> patientList; //病人列表数据
    private static Patient selectedPatient; //下拉列表选择的病人
    PatientAdapter patientAdapter; //病人列表数据适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_patient);

        initView(); //初始化界面引用
        initEvent(); //初始化事件
        initData(); //初始化界面数据
    }

    //点击回调
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.add_patient_button == id) {
            //跳转到添加病人页面并处理返回结果
            Intent intent = new Intent(SelectPatientActivity.this, AddPatientActivity.class);
            startActivityForResult(intent, ACTION_ADD_PATIENT);
        }
    }

    //初始化界面引用
    private void initView() {
        //初始化组件引用
        searchPatientSV = findViewById(R.id.sv_search_patient);
        patientRV = findViewById(R.id.lv_patient);
        addPatientBtn = findViewById(R.id.add_patient_button);

        //设置线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        patientRV.setLayoutManager(linearLayoutManager);
    }

    //初始化事件
    private void initEvent() {
        addPatientBtn.setOnClickListener(this);
        // 设置搜索文本监听
        searchPatientSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                patientAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    //初始化界面数据
    private void initData() {
        initPatientList();
    }

    /*
     * 初始化病人列表数据
     * @author Hadeslock
     * @time 2022/4/14 15:12
     */
    private void initPatientList() {
        //初始化病人列表
        String getPatientsUrl = BuildConfig.baseUrl + "patient/allPatients";
        HttpUtil.getRequest(getPatientsUrl, new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //解析响应，恢复病人列表
                if (response.body() != null) {
                    //设置病人列表
                    patientList = Patient.parsePatients(response);
                    //发送消息，设置数据源
                    Message message = new Message();
                    message.what = MSG_SET_PATIENT_ADAPTER;
                    mHandler.sendMessage(message);
                } else {
                    Log.e(TAG, "onResponse: 未获取到病人数据");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message message = new Message();
                message.what = MSG_NETWORK_FAILURE;
                mHandler.sendMessage(message);
            }
        });
    }

    //消息回调
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_SET_PATIENT_ADAPTER) {//设置列表数据源
                patientAdapter = new PatientAdapter(mHandler, patientList);
                patientRV.setAdapter(patientAdapter);
            } else if (what == MSG_NETWORK_FAILURE) {
                Toast.makeText(SelectPatientActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            } else if (MSG_PATIENT_ITEM_CLICK == what) {
                //确认选择病人，跳转到测量界面
                //传递参数
                Intent intent = new Intent();
                intent.putExtra(DeviceMeasureActivity.EXTRAS_SELECTED_PATIENT_ID, selectedPatient.getId());
                intent.putExtra(DeviceMeasureActivity.EXTRAS_SELECTED_PATIENT_NAME, selectedPatient.getName());
                setResult(RESULT_OK, intent);
                //结束活动
                finish();
            }
            return true;
        }
    });

    private static class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder>
            implements Filterable {

        private final Handler handler; //消息钩子
        private final List<Patient> patientList;
        private List<Patient> filterList;

        public PatientAdapter(Handler handler, List<Patient> patientList) {
            this.handler = handler;
            this.patientList = patientList;
            filterList = patientList;
        }

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_patient, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //确认选择病人，跳转到测量界面
                    int position = holder.getAdapterPosition();
                    selectedPatient = filterList.get(position);
                    Message message = new Message();
                    message.what = MSG_PATIENT_ITEM_CLICK;
                    handler.sendMessage(message);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            //获取信息
            Patient patient = filterList.get(position);
            String name = "姓名：" + patient.getName();
            String age = "年龄：" + patient.getAge();
            String gender = "性别：" + (patient.getGender() == 0 ? "男" : "女");
            String addr = "住址：" + patient.getPosition();
            //设置信息
            holder.patientNameTV.setText(name);
            holder.patientAgeTV.setText(age);
            holder.patientGenderTV.setText(gender);
            holder.patientAddressTV.setText(addr);
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        //返回过滤器
        @Override
        public Filter getFilter() {
            return new Filter() {
                //进行过滤
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String keywords = constraint.toString();
                    if (keywords.isEmpty()) {
                        filterList = patientList;
                    } else {
                        filterList = new ArrayList<>();
                        for (Patient patient : patientList) {
                            if (patient.getName().contains(keywords)) {
                                filterList.add(patient);
                            }
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filterList;
                    return filterResults;
                }

                //返回过滤结果
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {

            View itemView;
            TextView patientNameTV;
            TextView patientAgeTV;
            TextView patientGenderTV;
            TextView patientAddressTV;

            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                this.itemView = itemView;
                patientNameTV = itemView.findViewById(R.id.tv_patient_name);
                patientAgeTV = itemView.findViewById(R.id.tv_patient_age);
                patientGenderTV = itemView.findViewById(R.id.tv_patient_gender);
                patientAddressTV = itemView.findViewById(R.id.tv_patient_address);
            }
        }
    }
}