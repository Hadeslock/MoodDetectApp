package com.example.pc.lbs.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.pojo.Device;
import com.example.pc.lbs.utils.HttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewBoundDeviceActivity extends AppCompatActivity {

    // 消息码
    private static final int MSG_NETWORK_FAILURE = 1; //网络错误
    private static final int MSG_RECEIVE_DEVICES = 2; //接收到绑定的设备数据
    private static final int MSG_REQUEST_DEVICE_FAILURE = 3; //请求绑定设备数据失败
    private static final int MSG_UNBIND_DEVICE_BTN_CLICK = 4; //点击了解绑按钮
    private static final int MSG_UNBIND_DEVICE_SUCCESS = 5; //解绑成功消息
    private static final int MSG_UNBIND_DEVICE_FAILURE = 6; //解绑失败


    private List<Device> mBoundDeviceList = new ArrayList<>(); //设备列表数据

    private RecyclerView deviceRecycleView; //设备列表视图
    private static int deviceIdUnbind; //要解绑的设备的id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bound_device);

        initView();
        initData();
    }

    //初始化视图
    private void initView() {
        deviceRecycleView = findViewById(R.id.view_bound_device_recycle);
        //设置线性视图
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deviceRecycleView.setLayoutManager(layoutManager);
    }

    //初始化视图数据
    private void initData() {
        //请求已绑定设备数据
        String fetchDevicesUrl = BuildConfig.baseUrl + "device/allDevices";
        HttpUtil.getRequest(fetchDevicesUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //网络错误
                Message message = new Message();
                message.what = MSG_NETWORK_FAILURE;
                mHandler.sendMessage(message);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();
                Message message = new Message();
                if (200 == code) { //响应成功
                    message.what = MSG_RECEIVE_DEVICES;
                    //解析返回的响应
                    mBoundDeviceList = Device.parseDeviceListFromResponse(response);
                } else {
                    message.what = MSG_REQUEST_DEVICE_FAILURE;
                }
                //发送消息,校验结果并做相应的动作
                mHandler.sendMessage(message);
            }
        });
    }

    //消息回调
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (MSG_NETWORK_FAILURE == what) { //网络错误
                Toast.makeText(ViewBoundDeviceActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                return true;
            } else if (MSG_RECEIVE_DEVICES == what) { //接收到绑定的设备数据
                //解析到列表视图，同时设置解绑按钮回调事件。
                BoundDeviceAdapter boundDeviceAdapter = new BoundDeviceAdapter(mBoundDeviceList, mHandler);
                deviceRecycleView.setAdapter(boundDeviceAdapter);
                return true;
            } else if (MSG_REQUEST_DEVICE_FAILURE == what) {
                //请求绑定设备数据失败
                Toast.makeText(ViewBoundDeviceActivity.this, "请求绑定设备数据失败", Toast.LENGTH_SHORT).show();
                return true;
            } else if (MSG_UNBIND_DEVICE_BTN_CLICK == what) {
                String deleteDeviceUrl = BuildConfig.baseUrl + "device/" + deviceIdUnbind;
                HttpUtil.putRequest(deleteDeviceUrl, RequestBody.create(new byte[1]), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //网络错误
                        Message message = new Message();
                        message.what = MSG_NETWORK_FAILURE;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        int code = response.code();
                        Message message = new Message();
                        if (200 == code) { //响应成功
                            message.what = MSG_UNBIND_DEVICE_SUCCESS;
                        } else {
                            message.what = MSG_UNBIND_DEVICE_FAILURE;
                            //传递失败原因
                            Bundle bundle = new Bundle();
                            bundle.putString("message", response.message());
                            message.setData(bundle);
                        }
                        //发送消息,校验结果并做相应的动作
                        mHandler.sendMessage(message);
                    }
                });
                return true;
            } else if (MSG_UNBIND_DEVICE_SUCCESS == what) { //解绑成功
                Toast.makeText(ViewBoundDeviceActivity.this, "解绑成功", Toast.LENGTH_SHORT).show();
                initData();
                return true;
            } else if (MSG_UNBIND_DEVICE_FAILURE == what) { //解绑失败
                Bundle data = msg.getData();
                String failMessage = data.getString("message");
                Toast.makeText(ViewBoundDeviceActivity.this, failMessage, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }
    });

    //已绑定设备的列表适配器
    private static class BoundDeviceAdapter extends RecyclerView.Adapter<BoundDeviceAdapter.ViewHolder> {

        private final List<Device> deviceList; //设备列表数据
        private final Handler handler; //消息回调

        public BoundDeviceAdapter(List<Device> deviceList, Handler handler) {
            this.deviceList = deviceList;
            this.handler = handler;
        }

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bound_device_item, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            //为解绑按钮设置点击事件
            holder.unbindBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取要解绑的设备的id
                    int position = holder.getAdapterPosition();
                    deviceIdUnbind = deviceList.get(position).getId();
                    //发送消息
                    Message message = new Message();
                    message.what = MSG_UNBIND_DEVICE_BTN_CLICK;
                    handler.sendMessage(message);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            Device device = deviceList.get(position);
            holder.deviceNameTextView.setText(device.getDevice_name());
            holder.deviceAddressTextView.setText(device.getDevice_id());
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
        }

        //视图容器
        static class ViewHolder extends RecyclerView.ViewHolder {

            View deviceView; //整体的视图
            TextView deviceNameTextView; //名称的标签
            TextView deviceAddressTextView; //mac地址的标签
            Button unbindBtn; //解绑按钮

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                deviceView = itemView;
                deviceNameTextView = deviceView.findViewById(R.id.bound_device_name);
                deviceAddressTextView = deviceView.findViewById(R.id.bound_device_address);
                unbindBtn = deviceView.findViewById(R.id.bound_device_unbind_btn);
            }
        }
    }
}