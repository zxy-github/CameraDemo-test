package com.example.lenovo.camerademo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

//import com.tbruyelle.rxpermissions2.Permission;
//import com.tbruyelle.rxpermissions2.RxPermissions;
//
//import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {
    private CameraPreview mPreview;
    //判断设置列表是否开启
    private Boolean isSetting = false;
    //判断相机预览是否开启
    private Boolean isPreview = true;
    //判断相机是否在录制
    private Boolean isRecord = false;
//    //判断风格选择框是否显示
//    private Boolean isVisisble = false;
//    //是否选中风格图片
//    private Boolean isClick = false;
//
//    //整体风格显示框
//    private LinearLayout mGallery;
//    //风格图片来源
//    private int[] mImgIds;
//    //风格图片名称
//    private String[] mImgNames;
//    //单个风格图片显示
//    private LayoutInflater mInflater;

    //录像需要的权限
    private String[] VIDEO_PERMISSIONS = {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void checkPermissionRequestEach(){
        for (int i = 0; i < VIDEO_PERMISSIONS.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, VIDEO_PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{VIDEO_PERMISSIONS[i]}, 0/*requestCode*/);
            }
            while (ContextCompat.checkSelfPermission(this, VIDEO_PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {}
        }
    }

    //初始化相机
    private void initCamera(){
        /* 将CameraPreview加入到FrameLayout */
        mPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        /* 向SettingsFragment传递来自mPreview的相机 */
        SettingFragment.passCamera(mPreview.getCameraInstance());
        //获取相机的默认参数
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //判断并设置默认参数
        SettingFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        //应用修改后的mParameters到相机
        SettingFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    //onPause重载
    public void onPause() {
        super.onPause();
        mPreview = null;
    }

    public void onResume() {
        super.onResume();
        if (mPreview == null) {
            initCamera();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!android.os.Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"请安装SD卡！",Toast.LENGTH_SHORT).show();
        }

        //申请权限
        checkPermissionRequestEach();

        //初始化相机
        initCamera();

//        //初始化风格选择框
//        mInflater = LayoutInflater.from(this);
//        initStyleView();

        /* 添加设置按键 */
        Button btnSettings = (Button) findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
           //为设置按钮绑定点击监听，当点击设置按钮时，就显示设置菜单
            @Override
            public void onClick(View v) {
                //打开设置列表
                if(!isSetting){
                    isSetting = true;
                    getFragmentManager().beginTransaction().replace(R.id.camera_preview,
                            new SettingFragment()).addToBackStack(null).commit();
                }
                //关闭设置列表
                else{
                    isSetting = false;
                    getFragmentManager().popBackStack();
                }
            }
        });

//        //镜面翻转按键响应
//        Button btnInvert = (Button)findViewById(R.id.btn_invert);
//        btnInvert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPreview.onChange();
//            }
//        });

        //预览图显示窗
        final ImageView mediaPreview = (ImageView) findViewById(R.id.media_preview);
        /* 添加拍照响应 */
        final Button buttonCapturePhoto = (Button) findViewById(R.id.btn_capture_photo);
        buttonCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPreview){
                    mPreview.takePicture(mediaPreview);
                    buttonCapturePhoto.setText("预览");
                    isPreview = false;
                }else{
                    mPreview.resetCamera();
                    buttonCapturePhoto.setText("拍照");
                    isPreview = true;
                }


            }
        });

        /*添加视频响应*/
        final Button buttonCaptureVideo = (Button) findViewById(R.id.btn_capture_video);
        buttonCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecord) {
                    isRecord = false;
//                    mPreview.stopRecording(mediaPreview);
                    buttonCaptureVideo.setText("录像");
                    Toast.makeText(MainActivity.this,"录制结束",Toast.LENGTH_SHORT).show();
                } else {
                    isRecord = true;
                    buttonCaptureVideo.setText("停止");
                    Toast.makeText(MainActivity.this,"开始录制",Toast.LENGTH_SHORT).show();
//                    mPreview.startRecording();
                }
            }
        });

        //预览图全屏显示
        mediaPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * 通过Data和Type向这个Intent传递拍到的照片或视频的URI和MIME
                * 使得点击后退时能够返回到MainActivity*/
                Intent intent = new Intent(MainActivity.this, ShowPhotoVideo.class);
                intent.setDataAndType(mPreview.getOutputMediaFileUri(), mPreview.getOutputMediaFileType());
                startActivityForResult(intent, 0);
            }
        });


//        final HorizontalScrollView styleview = (HorizontalScrollView)findViewById(R.id.styleView);
        Button btn = (Button)findViewById(R.id.btn_stylization);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!isVisisble){
//                    isVisisble = true;
//                    //设置风格列表显示
//                    styleview.setVisibility(View.VISIBLE);
//                }else{
//                    isVisisble = false;
//                    //设置风格列表显示
//                    styleview.setVisibility(View.GONE);
//                }
                Intent intent = new Intent(MainActivity.this, PictureStylization.class);
                intent.setDataAndType(mPreview.getOutputMediaFileUri(), mPreview.getOutputMediaFileType());
                startActivityForResult(intent, 0);
            }
        });
    }

//    //初始化风格图片
//    private void initStyleView() {
//        mImgIds = new int[]{R.drawable.candy, R.drawable.composition_vii, R.drawable.la_muse,
//                R.drawable.feathers, R.drawable.mosaic, R.drawable.starry_night,
//                R.drawable.the_scream, R.drawable.udnie, R.drawable.wave_crop
//        };
//
//        mImgNames = new String[]{"tangguo","composition_vii","la_muse","feathers","mosaic",
//                "starry_night","the_scream","udine","wave"
//        };
//
//        mGallery = (LinearLayout) findViewById(R.id.id_gallery);
//
//        for (int i = 0; i < mImgIds.length; i++) {
//            View view = mInflater.inflate(R.layout.activity_index_gallery,
//                    mGallery, false);
//            final ImageView img = (ImageView) view
//                    .findViewById(R.id.id_index_gallery_item_image);
//            img.setImageResource(mImgIds[i]);
//            TextView txt = (TextView) view
//                    .findViewById(R.id.id_index_gallery_item_text);
//            txt.setText(mImgNames[i]);
//
//            //每张图片设置点击的响应
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(isClick){
//                        isClick = false;
//                        img.setBackgroundColor(Color.parseColor("#ffffff"));
//                    }else{
//                        isClick = true;
//                        img.setBackgroundColor(Color.parseColor("#ff0000"));
//                    }
//                }
//            });
//            mGallery.addView(view);
//        }
//    }
}
