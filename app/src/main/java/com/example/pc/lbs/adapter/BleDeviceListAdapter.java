package com.example.pc.lbs.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.pc.lbs.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Hadeslock
 * Created on 2022/1/22 14:17
 * Email: hadeslock@126.com
 * Desc: 蓝牙适配器列表类，用于保存扫描到的蓝牙设备
 */
public class BleDeviceListAdapter extends BaseAdapter {
    private Context mContext;
    private List<BluetoothDevice> mBluetoothDeviceList;
    private List<Integer> mRssiList;

    public BleDeviceListAdapter(Context mContext) {
        this.mContext = mContext;
        this.mBluetoothDeviceList = new ArrayList<>();
        this.mRssiList = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device) {
        if (!isDeviceExist(device)) {
            mBluetoothDeviceList.add(device);
        }
    }

    public boolean isDeviceExist(BluetoothDevice device) {
        return mBluetoothDeviceList.contains(device);
    }

    public BluetoothDevice getDevice(int position) {
        return mBluetoothDeviceList.get(position);
    }

    public void addRssi(int rssi) {
        mRssiList.add(rssi);
    }

    public int getRssi(int position) {
        return mRssiList.get(position);
    }

    public void clear() {
        this.mBluetoothDeviceList.clear();
        this.mRssiList.clear();
    }

    @Override
    public int getCount() {
        return mBluetoothDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBluetoothDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_devices_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice device = (BluetoothDevice) getItem(position);
        viewHolder.deviceName.setText(device.getName());
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText(String.valueOf(mRssiList.get(position)));
        return convertView;
    }


    class ViewHolder {
        public TextView deviceName;
        public TextView deviceAddress;
        public TextView deviceRssi;

        public ViewHolder(View view) {
            deviceName = view.findViewById(R.id.name);
            deviceAddress = view.findViewById(R.id.introduce);
            deviceRssi = view.findViewById(R.id.rssi);
        }
    }
}
