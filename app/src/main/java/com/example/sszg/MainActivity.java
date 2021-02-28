package com.example.sszg;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.google.zxing.activity.CaptureActivity;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.google.gson.Gson;
import com.hdgq.locationlib.LocationOpenApi;
import com.hdgq.locationlib.entity.ShippingNoteInfo;
import com.hdgq.locationlib.listener.OnResultListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private final int mRequestCode = 100;
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    private Uri imageUri;
    List<String> mPermissionList = new ArrayList<>();

    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    //扫描成功返回码
    private int RESULT_OK = 0xA1;

    private class JsBridge {
        @JavascriptInterface
        public void showToast(String arg){
            Toast.makeText(MainActivity.this, arg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BridgeWebView mw = (BridgeWebView) findViewById(R.id.myweb);
        mw.getSettings().setJavaScriptEnabled(true);
//        mw.loadUrl("http://gs.wholexy.cn/index.html");
        mw.loadUrl("file:///android_asset/demo/views/shippingLists.html");
        /*w.getSettings().setAllowFileAccess(true);
        mw.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mw.getSettings().setAllowFileAccessFromFileURLs(true);
        mw.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mw.getSettings().setDomStorageEnabled(true);
        mw.getSettings().setLoadWithOverviewMode(true);
        mw.getSettings().setAllowContentAccess(true);
        mw.getSettings().setAllowFileAccess(true);*/
        mw.addJavascriptInterface(new JsBridge(),"Android");

        /*mw.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.confirm();
                            }
                        });
                b.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                result.cancel();
                            }
                        });
                b.setCancelable(false);
                b.create();
                b.show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                                      String defaultValue, final JsPromptResult result) {
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                takePhoto();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                takePhoto();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                takePhoto();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                takePhoto();
            }
        });

        mw.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写WebViewClient的shouldOverrideUrlLoading()方法
                //使用WebView加载显示url
                view.loadUrl(url);
                //返回true
                return super.shouldOverrideUrlLoading(view, url);
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
            }

        });*/

        initPermission();
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

        mw.registerHandler("startTransport", new BridgeHandler() {
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

        mw.registerHandler("stopTransport", new BridgeHandler() {
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

        mw.registerHandler("scanCode", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                //打开二维码扫描界面
//                if(CommonUtil.isCameraCanUse()){
//                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
//                    startActivityForResult(intent, REQUEST_CODE);
//                }else{
//                    Toast.makeText(MainActivity.this,"请打开此应用的摄像头权限！",Toast.LENGTH_SHORT).show();
//                }
//                Toast.makeText(MainActivity.this,"scanCode",Toast.LENGTH_SHORT).show();

                Log.d("demo", "来了老弟！");
                String noteNumber = "noteNumber";
                String serialNumber = "serialNumber";
                String startCode = "startCode";
                String endCode = "endCode";
                JSONObject obj = new JSONObject();
                obj.put("noteNumber", noteNumber);
                obj.put("serialNumber", serialNumber);
                obj.put("startCode", startCode);
                obj.put("endCode", endCode);
                function.onCallBack(obj.toString());
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
            //权限已通过
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestCode==requestCode){
            for (int i=0;i<grantResults.length;i++){
                if (grantResults[i]==-1){
                    break;
                }
            }
        } else {
            //权限已通过
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                Log.e("Mr.Kang", "onActivityResult: "+result);
                if (result == null) {
                    mUploadMessage.onReceiveValue(imageUri);
                    mUploadMessage = null;

                    Log.e("Mr.Kang", "onActivityResult: "+imageUri);
                } else {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }
            }
        }else if(resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("qr_scan_result");
            //将扫描出的信息显示出来
            Toast.makeText(MainActivity.this,scanResult,Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("null")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE
                || mUploadCallbackAboveL == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{imageUri};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }

        return;
    }


    private void takePhoto() {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        imageUri = Uri.fromFile(file);

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);

        }
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        MainActivity.this.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

}