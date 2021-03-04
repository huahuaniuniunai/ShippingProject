package com.example.sszg;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.hdgq.locationlib.LocationOpenApi;
import com.hdgq.locationlib.entity.ShippingNoteInfo;
import com.hdgq.locationlib.listener.OnResultListener;
import com.yzq.zxinglibrary.common.Constant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final int mRequestCode = 100;
    private final int REQUEST_CODE_SCAN = 0X01;
    List<String> mPermissionList = new ArrayList<>();
    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    private BridgeWebView mWebView;

    private class JsBridge {
        @JavascriptInterface
        public void showToast(String arg){
            Toast.makeText(MainActivity.this, arg, Toast.LENGTH_SHORT).show();
        }

        // 安卓原生与h5互调方法定义
        @JavascriptInterface //js接口声明
        public void takePhoto() {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class); //打开扫一扫
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.d("demo", content);
                String method = "javascript:testResult('" + content + "')";
                mWebView.loadUrl(method);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();
        mWebView = (BridgeWebView) findViewById(R.id.myweb);
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.loadUrl("http://gs.wholexy.cn/index.html");
        mWebView.loadUrl("file:///android_asset/demo/views/shippingLists.html");
        mWebView.addJavascriptInterface(new JsBridge(),"Android");

        LocationOpenApi.init(MainActivity.this,
                "com.example.sszg",
                "cb144f03be6e465cb7df4191151a3b1e128b536a26b24cb28f35ca8f9ec5f13f",
                "35032257162e863d494f",
                "debug",
                new OnResultListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("demo", "初始化成功！");
                        /*ShippingNoteInfo[] infoList = new ShippingNoteInfo[1];
//                        infoList = new ShippingNoteInfo[10];
                        for (int i = 0; i < infoList.length; i++) {
                            infoList[i] = new ShippingNoteInfo();
                        }

                        ShippingNoteInfo info = new ShippingNoteInfo();
                        info.setShippingNoteNumber("2020090311523000879");
                        info.setSerialNumber("20200905191238B7A9C");
                        info.setStartCountrySubdivisionCode("150100");
                        info.setEndCountrySubdivisionCode("150102");

                        infoList[0] = info;

                        LocationOpenApi.start(MainActivity.this, infoList, new OnResultListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(MainActivity.this,"start success",Toast.LENGTH_SHORT).show();
                                Log.i("start","start success");
                            }

                            @Override
                            public void onFailure(String s, String s1) {
                                Toast.makeText(MainActivity.this,"start error"+s+"--"+s1,Toast.LENGTH_SHORT).show();
                                Log.i("start","start error");
                            }
                        });*/
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        Log.d("demo", "初始化失败(" + s + "+" + s1 + ")");
                    }
                });

        mWebView.registerHandler("startTransport", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                JSONObject object = JSONObject.parseObject(data);
                String noteNumber = object.getString("noteNumber");
                String serialNumber = object.getString("serialNumber");
                String startCode = object.getString("startCode");
                String endCode = object.getString("endCode");

                ShippingNoteInfo info = new ShippingNoteInfo();
                info.setShippingNoteNumber(noteNumber);
                info.setSerialNumber(serialNumber);
                info.setStartCountrySubdivisionCode(startCode);
                info.setEndCountrySubdivisionCode(endCode);
                ShippingNoteInfo[] infos = {info};

//                ShippingNoteInfo[] infoList = new ShippingNoteInfo[10];
//                for (int i = 0; i < infoList.length; i++) {
//                    infoList[i] = new ShippingNoteInfo();
//                }
//                infoList[0] = info;
                Log.d("demo", "------货物启运-----");
                Log.d("demo", infos[0].getShippingNoteNumber());
                Log.d("demo", infos[0].getSerialNumber());
                Log.d("demo", infos[0].getStartCountrySubdivisionCode());
                Log.d("demo", infos[0].getEndCountrySubdivisionCode());

                LocationOpenApi.start(MainActivity.this, infos, new OnResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this,"start success",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        Log.d("demo", "sdk定位失败详情：" + s + s1);
                        Toast.makeText(MainActivity.this,"start error",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mWebView.registerHandler("stopTransport", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                JSONObject object = JSONObject.parseObject(data);
                String noteNumber = object.getString("noteNumber");
                String serialNumber = object.getString("serialNumber");

                ShippingNoteInfo info = new ShippingNoteInfo();
                info.setShippingNoteNumber(noteNumber);
                info.setSerialNumber(serialNumber);
                ShippingNoteInfo[] infos = {info};

//                ShippingNoteInfo[] infoList = new ShippingNoteInfo[10];
//                for (int i = 0; i < infoList.length; i++) {
//                    infoList[i] = new ShippingNoteInfo();
//                }
//                infoList[0] = info;

                Log.d("demo", "------货物送达-----");
                Log.d("demo", infos[0].getShippingNoteNumber());
                Log.d("demo", infos[0].getSerialNumber());

                LocationOpenApi.stop(MainActivity.this, infos, new OnResultListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this,"start success",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String s, String s1) {
                        Log.d("demo", "sdk定位失败详情：" + s + s1);
                        Toast.makeText(MainActivity.this,"start error",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private void initPermission(){
        mPermissionList.clear();
        for (int i = 0;i<permissions.length;i++){
            if (ContextCompat.checkSelfPermission(this,permissions[i])!=
                    PackageManager.PERMISSION_GRANTED){
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.size()>0){
            ActivityCompat.requestPermissions(this,permissions,mRequestCode);
        }else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestCode==requestCode){
            for (int i=0;i<grantResults.length;i++){
                if (grantResults[i]==-1){
                    break;
                }
            }
        } else {

        }
    }
}