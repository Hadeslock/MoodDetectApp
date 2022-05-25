package com.example.pc.lbs.module;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: Hadeslock
 * Created on 2022/5/13 19:02
 * Email: hadeslock@126.com
 * Desc: 接收到的数据类
 * 继承Observable，实现更新时通知UI更新和本地文件存储
 */

public class ReceivedData {

    //电位数据以list的形式保存
    private List<String> potentialList;
    private List<Boolean> changedList;

    private OnDataListUpdateListener mOnDataListUpdateListener;

    /*
     * 构造器
     * @author hadeslock
     * @date 2022/5/13 18:53
     * @param n 电位数据是有几个设备
     * @return
     */
    public ReceivedData(int n, OnDataListUpdateListener listener) {
        potentialList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            potentialList.add("");
        }
        changedList = new CopyOnWriteArrayList<>();
        for (int i = 0; i < n; i++) {
            changedList.add(false);
        }
        setOnDataListUpdateListener(listener);
    }

    /*
     * 设置指定位置的电位数据，如果所有项都被更新过一次，就通知观察者
     * @author hadeslock
     * @date 2022/5/13 18:59
     * @param index 序号
     * @param potential 电位数据
     * @return void
     */
    public void setPotentialList(int index, String potential) {
        potentialList.set(index, potential);
        changedList.set(index, true);
        //检查是否全部更新
        for (boolean change : changedList) {
            if (!change) return;
        }
        //已经全部更新，通知
        mOnDataListUpdateListener.onDataListUpdate(potentialList);
        //重置状态标记
        for (int i = 0; i < changedList.size(); i++) {
            changedList.set(i, false);
        }
    }

    public interface OnDataListUpdateListener {
        void onDataListUpdate(List<String> datalist);
    }

    private void setOnDataListUpdateListener(OnDataListUpdateListener listener) {
        mOnDataListUpdateListener = listener;
    }
}
