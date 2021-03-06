package com.example.pc.lbs.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.activity.LoginActivity;
import com.example.pc.lbs.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Author: Hadeslock
 * Created on 2022/5/1 18:20
 * Email: hadeslock@126.com
 * Desc: 首页我的功能页
 */
public class MeFragment extends Fragment {

    //本fragment的根视图
    private View rootView;
    //功能列表
    private ListView funcListView;

    public MeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_me, container, false);

        initView();
        initData();
        initEvent();

        return rootView;
    }

    //初始化组件引用
    private void initView() {
        funcListView = rootView.findViewById(R.id.frag_me_func_list);
    }

    //初始化界面数据
    private void initData() {
        ArrayList<String> funcListData = new ArrayList<>();
        funcListData.add("查看个人信息（todo）");
        funcListData.add("设置（todo）");
        funcListData.add("发布版本：V" + BuildConfig.VERSION_NAME);
        funcListData.add("注销");
        funcListView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, funcListData));
    }

    //初始化组件事件
    private void initEvent() {
        funcListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Class<?> targetClass = null;
                switch (i) {
                    case 3://注销登录
                        SharedPreferences.Editor editor =
                                getActivity().getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putBoolean("isLogin", false);
                        editor.apply();
                        targetClass = LoginActivity.class;
                        break;
                    default:
                        break;
                }
                if (targetClass != null) {
                    Intent intent = new Intent(getActivity(), targetClass);
                    startActivity(intent);
                    //如果是要跳转到登录界面，说明是注销操作，本页面需要被关闭
                    if (targetClass == LoginActivity.class) {
                        getActivity().finish();
                    }
                }
            }
        });
    }
}