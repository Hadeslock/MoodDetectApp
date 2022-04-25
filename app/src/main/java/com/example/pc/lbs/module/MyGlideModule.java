package com.example.pc.lbs.module;

import android.content.Context;
import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.example.pc.lbs.TheUtils.HttpUtil;
import okhttp3.Call;

import java.io.InputStream;

/**
 * Author: Hadeslock
 * Created on 2022/4/12 20:04
 * Email: hadeslock@126.com
 * Desc: <a href="https://muyangmin.github.io/glide-docs-cn/doc/configuration.html#%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F">https://muyangmin.github.io/glide-docs-cn/doc/configuration.html#%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F</a>
 * 从 Glide 4.9.0 开始，在某些情形下必须完成必要的设置 (setup)。
 * 对于应用程序（application），仅当以下情形时才需要做设置：
 * 使用一个或更多集成库
 * 修改 Glide 的配置(configuration)（磁盘缓存大小/位置，内存缓存大小等）
 * 扩展 Glide 的API。
 */

@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context,
                                   @NonNull Glide glide, @NonNull Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory) HttpUtil.client));
    }
}
