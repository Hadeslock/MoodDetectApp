package com.example.pc.lbs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.example.pc.lbs.Activities.TestActivity;
import com.example.pc.lbs.Activities.ViewUnuploadRecordActivity;
import com.example.pc.lbs.R;

import java.util.ArrayList;

/**
 * Author: Hadeslock
 * Created on 2022/5/1 18:10
 * Email: hadeslock@126.com
 * Desc: 首页查看记录功能页
 */
public class ViewFragment extends Fragment {

    private View rootView;
    private ListView funcListView;

    public ViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_view, container, false);

        //初始化功能列表
        funcListView = rootView.findViewById(R.id.frag_view_func_list);
        initData();
        initEvent();

        return rootView;
    }

    //初始化界面数据
    private void initData() {
        ArrayList<String> funcListData = new ArrayList<>();
        funcListData.add("查看测试记录");
        funcListData.add("查看未上传测试记录");
        funcListView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, funcListData));
    }

    //初始化组件事件
    private void initEvent() {
        funcListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Class<?> targetClass = null;
                switch (i) {
                    case 0:
                        targetClass = TestActivity.class;
                        break;
                    case 1:
                        targetClass = ViewUnuploadRecordActivity.class;
                        break;
                    default:
                        break;
                }
                if (targetClass != null) {
                    Intent intent = new Intent(getActivity(), targetClass);
                    startActivity(intent);
                }
            }
        });
    }
}