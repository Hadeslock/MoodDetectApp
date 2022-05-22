package com.example.pc.lbs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.pc.lbs.R;
import org.jetbrains.annotations.NotNull;

public class AppendCommentDialogFragment extends DialogFragment {

    private EditText measureCommentET;

    public interface AppendCommentDialogListener {
        public void onDialogPositiveClick(String comment);
    }

    AppendCommentDialogListener listener;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            listener = (AppendCommentDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context +
                    " must implement AppendCommentDialogListener");
        }
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 使用 Builder 类进行方便的对话框构建
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //获取指定的自定义界面的引用
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_measure_comment, null);
        measureCommentET = view.findViewById(R.id.et_measure_comment);
        //获取传输的数据
        Bundle arguments = getArguments();
        String comment = arguments.getString("comment");
        measureCommentET.setText(comment);
        //构建dialog
        builder.setView(view)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(measureCommentET.getText().toString());
                    }
                });
        return builder.create();

    }
}
