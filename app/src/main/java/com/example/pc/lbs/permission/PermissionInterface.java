package com.example.pc.lbs.permission;

/**
 * Author: Hadeslock
 * Created on 2022/4/14 13:39
 * Email: hadeslock@126.com
 * Desc: 权限请求接口 <a href="https://blog.csdn.net/lin_dianwei/article/details/79025324">https://blog.csdn.net/lin_dianwei/article/details/79025324</a>
 */
public interface PermissionInterface {
    /**
     * 可设置请求权限请求码
     */
    int getPermissionsRequestCode();

    /**
     * 设置需要请求的权限
     */
    String[] getPermissions();

    /**
     * 请求权限成功回调
     */
    void requestPermissionsSuccess();

    /**
     * 请求权限失败回调
     */
    void requestPermissionsFail();
}
