package com.example.pc.lbs.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.example.pc.lbs.BuildConfig;
import com.example.pc.lbs.R;
import com.example.pc.lbs.utils.FileUtils;
import com.example.pc.lbs.utils.HttpUtil;
import com.example.pc.lbs.pojo.RespBean;
import okhttp3.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: Hadeslock
 * Created on 2022/4/29 16:14
 * Email: hadeslock@126.com
 * Desc: 查看所有未上传的测试记录的活动
 */
public class ViewUnuploadRecordActivity extends AppCompatActivity {

    //消息码
    private static final int MSG_NETWORK_FAILURE = 1;
    private static final int MSG_UPLOAD_RECORD_SUCCESS = 2;
    private static final int MSG_UPLOAD_RECORD_FAILURE = 3;

    //private List<String> fileList = new ArrayList<>(); //文件名string列表
    // 使用ArrayList会在遍历的时候出现java.util.ConcurrentModificationException，
    // 但没有对列表数据进行增删为什么会导致这个问题呢？改用CopyOnWriteArrayList解决
    private List<String> fileList = new CopyOnWriteArrayList<>(); //文件名string列表

    //组件引用
    private ListView fileListView; //文件名列表
    private Button uploadBtn; //上传按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_unupload_record);

        //初始化
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //检测输出目录下的所有文件
        File filePath = new File(FileUtils.baseDirPath);
        File[] files = filePath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String fileName = pathname.getName();
                return fileName.startsWith("tbs");
            }
        });
        //添加符合的文件
        for (File file : files) {
            fileList.add(file.getName());
        }
    }

    //初始化界面
    private void initView() {
        //初始化组件引用
        fileListView = findViewById(R.id.unupload_file_list);
        uploadBtn = findViewById(R.id.unupload_upload_btn);

        //设置点击时间
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开一个子线程上传文件
                new Thread(uploadTask).start();
            }
        });

        //设置列表数据源
        setListViewAdapter(fileListView, fileList);
    }

    //为列表设置数据源
    private void setListViewAdapter(ListView view, List<String> fileList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        view.setAdapter(adapter);
    }

    //上传文件的操作，因为涉及到网络操作，必须放在子线程中进行
    private final Runnable uploadTask = new Runnable() {
        @Override
        public void run() {
            for (String file : fileList) {
                String filePath = FileUtils.baseDirPath + file;
                String uploadUrl = BuildConfig.baseUrl + "record/upload";
                //构建请求参数
                MultipartBody multipartBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", filePath,
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(filePath)))
                        .build();
                //构建请求
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(multipartBody)
                        .build();
                //同步发送请求，一个文件发送完再发送下一个
                try {
                    //发送请求
                    Response response = HttpUtil.client.newCall(request).execute();
                    //解析返回的响应
                    RespBean respBean = RespBean.parseResponse(response);
                    //分情况处理
                    Message message = new Message();
                    //传递上传的文件名
                    Bundle bundle = new Bundle();
                    bundle.putString("fileName", file);
                    message.setData(bundle);

                    long code = respBean.getCode();
                    if (200 == code) {
                        //上传成功
                        message.what = MSG_UPLOAD_RECORD_SUCCESS;
                    } else {
                        //上传失败
                        message.what = MSG_UPLOAD_RECORD_FAILURE;
                        //传递响应的消息
                        bundle.putString("responseMsg", respBean.getMessage());
                        message.setData(bundle);
                    }
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    //发送网络失败消息
                    Message message = new Message();
                    message.what = MSG_NETWORK_FAILURE;
                    mHandler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }
    };

    //消息回调
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            String fileName = data.getString("fileName");
            String responseMsg = data.getString("responseMsg");
            int what = msg.what;
            if (MSG_NETWORK_FAILURE == what) {
                Toast.makeText(ViewUnuploadRecordActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                return true;
            } else if (MSG_UPLOAD_RECORD_SUCCESS == what) {
                Toast.makeText(ViewUnuploadRecordActivity.this, fileName + "上传成功", Toast.LENGTH_SHORT).show();
                //重命名文件
                File originFile = new File(FileUtils.baseDirPath + fileName);
                assert fileName != null;
                String newFileName = FileUtils.baseDirPath + fileName.substring(4);
                if (!originFile.renameTo(new File(newFileName))) {
                    Toast.makeText(ViewUnuploadRecordActivity.this,
                            "重命名文件出错", Toast.LENGTH_SHORT).show();
                } else {
                    //删除列表的数据
                    fileList.remove(fileName);
                    setListViewAdapter(fileListView, fileList);
                }
                return true;
            } else if (MSG_UPLOAD_RECORD_FAILURE == what) {
                Toast.makeText(ViewUnuploadRecordActivity.this, fileName + "上传失败\n" + responseMsg, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }
    });
}