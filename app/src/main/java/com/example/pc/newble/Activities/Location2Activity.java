package com.example.pc.newble.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.pc.newble.R;

import java.util.ArrayList;
import java.util.List;

public class Location2Activity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());


        setContentView(R.layout.activity_location2);



        positionText = (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(Location2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Location2Activity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(Location2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Location2Activity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }



    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
            currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
            currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");

            currentPosition.append("地址： ").append(location.getAddrStr()).append("\n");// 地址信息

           // currentPosition.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
           // currentPosition.append(location.getUserIndoorState());
           // currentPosition.append("\nDirection(not all devices have value): ");
           // currentPosition.append(location.getDirection());// 方向
           // currentPosition.append("\nlocationdescribe: ");
          //  currentPosition.append(location.getLocationDescribe());// 位置语义化信息

            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                currentPosition.append("\nspeed : ");
                currentPosition.append(location.getSpeed());// 速度 单位：km/h
                currentPosition.append("\nsatellite : ");
                currentPosition.append(location.getSatelliteNumber());// 卫星数目
                currentPosition.append("\nheight : ");
                currentPosition.append(location.getAltitude());// 海拔高度 单位：米
                currentPosition.append("\ngps status : ");
                currentPosition.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                currentPosition.append("\ndescribe : ");
                currentPosition.append("gps定位成功");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                // 运营商信息
                if (location.hasAltitude()) {// *****如果有海拔高度*****
                    currentPosition.append("\nheight : ");
                    currentPosition.append(location.getAltitude());// 单位：米
                }
              //  currentPosition.append("\noperationers : ");// 运营商信息
              //  currentPosition.append(location.getOperators());
                currentPosition.append("\ndescribe : ");
                currentPosition.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                currentPosition.append("\ndescribe : ");
                currentPosition.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                currentPosition.append("\ndescribe : ");
                currentPosition.append("服务端网络定位失败");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                currentPosition.append("\ndescribe : ");
                currentPosition.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                currentPosition.append("\ndescribe : ");
                currentPosition.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            positionText.setText(currentPosition);


        }

    }
}
