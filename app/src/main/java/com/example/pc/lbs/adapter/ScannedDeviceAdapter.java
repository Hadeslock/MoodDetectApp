package com.example.pc.lbs.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.pc.lbs.R;

import java.util.ArrayList;
import java.util.List;

public class ScannedDeviceAdapter extends BaseAdapter {

    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private ViewHolder holder;

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i--);
            }
        }
    }

    public void clearUnconnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i--);
            }
        }
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size()) {
            return null;
        }
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(parent.getContext(), R.layout.item_scanned_device, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        BleDevice bleDevice = getItem(position);
        if (bleDevice != null) {
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            holder.deviceNameTV.setText(bleDevice.getName());
            holder.deviceAddressTV.setText(bleDevice.getMac());
            holder.deviceRssiTV.setText(String.valueOf(bleDevice.getRssi()));
            if (isConnected) {
                holder.connectDeviceBtn.setText("断开连接");
            } else {
                holder.connectDeviceBtn.setText("连接");
            }
        }


        //设置事件
        holder.connectDeviceBtn.setOnClickListener(v -> {
            if (listener != null) {
                boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
                if (isConnected) {
                    listener.onDisConnect(bleDevice);
                } else {
                    listener.onConnect(bleDevice);
                }
            }
        });

        return convertView;
    }

    public int getIndex(BleDevice bleDevice) {
        return bleDeviceList.indexOf(bleDevice);
    }

    public static class ViewHolder {

        private View rootView;
        private TextView deviceNameTV;
        private TextView deviceAddressTV;
        private TextView deviceRssiTV;
        private Button connectDeviceBtn;

        public ViewHolder(@NonNull View itemView) {
            rootView = itemView;
            deviceNameTV = itemView.findViewById(R.id.tv_scanned_device_name);
            deviceAddressTV = itemView.findViewById(R.id.tv_scanned_device_address);
            deviceRssiTV = itemView.findViewById(R.id.tv_scanned_device_rssi);
            connectDeviceBtn = itemView.findViewById(R.id.btn_connect_device);
        }
    }

    public interface OnScannedDeviceClickListener {
        void onConnect(BleDevice bleDevice);

        void onDisConnect(BleDevice bleDevice);
    }

    private OnScannedDeviceClickListener listener;

    public void setOnScannedDeviceClickListener(OnScannedDeviceClickListener listener) {
        this.listener = listener;
    }
}
