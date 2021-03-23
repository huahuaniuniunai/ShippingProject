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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;// 适配5.0以上设备
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调
    private final int mRequestCode = 100;
    private final int REQUEST_CODE_SCAN = 0X01;
    private List<String> mPermissionList = new ArrayList<>();
    private BridgeWebView mWebView;
    private Uri imageUri;

    private class JsBridge {
        // 安卓原生与h5互调方法定义
        @JavascriptInterface //js接口声明
        public void showToast(String arg){
            Toast.makeText(MainActivity.this, arg, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
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
                String method = "javascript:testResult('" + content + "')";

                // Android 4.4以后版本才可使用evaluateJavascript()，调用有返回值js方法
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.evaluateJavascript(method, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            //此处为 js 返回的结果
                            Log.d("demo", "扫码结果：" + value);
                        }
                    });
                } else {
                    mWebView.loadUrl(method);// Android调用没有返回值js方法
                }
            }
        }

        // 打开相机、相册回调
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
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();
        mWebView = (BridgeWebView) findViewById(R.id.myweb);
        mWebView.getSettings().setJavaScriptEnabled(true);// 设置是否允许WebView支持JavaScript交互（默认是不允许）
        mWebView.getSettings().setAllowFileAccess(true);//设置可以访问文件,使用File协议
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//设置支持通过JS打开新窗口或弹框
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);// 设置是否允许通过file url加载的Js代码读取其他的本地文件,在Android 4.1前默认允许,在Android 4.1后默认禁止
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);// 设置是否允许通过file url加载的Javascript可以访问其他的源(包括http、https等源)，在Android 4.1前默认允许（setAllowFileAccessFromFileURLs()不起作用）在Android 4.1后默认禁止
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setSavePassword(false);
//        mWebView.loadUrl("http://gs.wholexy.cn/index.html");
        mWebView.loadUrl("file:///android_asset/demo/views/shippingLists.html");
        mWebView.addJavascriptInterface(new JsBridge(),"Android");

        // setWebChromeClient主要处理解析，渲染网页等浏览器做的事情(浏览器做的事情)；如辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
        // WebViewClient是帮助WebView处理各种通知、请求事件的
        mWebView.setWebChromeClient(new WebChromeClient() {
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
                takePhotos();
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                takePhotos();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                takePhotos();
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                takePhotos();
            }
        });

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

    private void takePhotos() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mRequestCode == requestCode){
            for (int i=0;i<grantResults.length;i++){
                if (grantResults[i]==-1){
                    break;
                }
            }
        } else {

        }
    }
}