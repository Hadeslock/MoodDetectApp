package com.example.pc.lbs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.pc.lbs.R;
import org.jetbrains.annotations.NotNull;

public class KeyTimeDialogFragment extends DialogFragment {

    private EditText keyTimeNoteET;

    public interface KeyTimeDialogListener {
        public void onDialogPositiveClick(int position, String note);
    }

    KeyTimeDialogListener listener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        // 验证宿主活动是否实现了回调接口
        try {
            // 实例化 NoticeDialogListener 以便我们可以向主机发送事件
            listener = (KeyTimeDialogListener) context;
        } catch (ClassCastException e) {
            // Activity 没有实现接口，抛出异常
            throw new ClassCastException(context
                    + " must implement KeyTimeDialogListener");
        }
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 使用 Builder 类进行方便的对话框构建
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //获取指定的自定义界面的引用
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_keytime, null);
        keyTimeNoteET = view.findViewById(R.id.et_key_time_note);
        //获取传输的数据
        Bundle arguments = getArguments();
        final int position = arguments.getInt("position");
        keyTimeNoteET.setText(arguments.getString("keyTimeVal"));
        //构建dialog
        builder.setView(view)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(position, keyTimeNoteET.getText().toString());
                    }
                });
        return builder.create();
    }
}
