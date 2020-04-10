package com.example.lenovo.camerademo;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

/**
 * Created by LENOVO on 2020/4/7.
 */

public class PictureStylization extends Activity {
    //判断风格选择框是否显示
//    private Boolean isVisisble = false;
    //是否选中风格图片
    private Boolean isClick = false;

    //整体风格显示框
    private LinearLayout mGallery;
    //风格图片来源
    private int[] mImgIds;
    //风格图片名称
    private String[] mImgNames;
    //单个风格图片显示
    private LayoutInflater mInflater;

    private void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylization);

        initPython();

        //初始化风格选择框
        mInflater = LayoutInflater.from(this);
        mImgIds = new int[]{R.drawable.candy, R.drawable.composition_vii, R.drawable.la_muse,
                R.drawable.feathers, R.drawable.mosaic, R.drawable.starry_night,
                R.drawable.the_scream, R.drawable.udnie, R.drawable.wave_crop
        };

        mImgNames = new String[]{"tangguo","composition_vii","la_muse","feathers","mosaic",
                "starry_night","the_scream","udine","wave"
        };

//        RelativeLayout imgDisplay = (RelativeLayout)findViewById(R.id.img_display);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        final ImageView imageView = (ImageView)findViewById(R.id.imageView);
        VideoView videoView = (VideoView)findViewById(R.id.videoView);

        //指定路径
        final Uri uri = getIntent().getData();

        //如果数据为照片
        if(getIntent().getType().equals("image/*")){
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            imageView.setImageURI(uri);
//            ImageView view = new ImageView(this);
//            view.setImageURI(uri);
//            view.setLayoutParams(layoutParams);
//            imgDisplay.addView(view);
        }
        //如果为视频
        else{
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            MediaController mc = new MediaController(this);
//            VideoView view = new VideoView(this);
            mc.setAnchorView(videoView);
            mc.setMediaPlayer(videoView);
            videoView.setMediaController(mc);
            videoView.setVideoURI(uri);
            videoView.start();
//            view.setLayoutParams(layoutParams);
//            imgDisplay.addView(view);
        }

        //风格化按键
//        final HorizontalScrollView styleview = (HorizontalScrollView)findViewById(R.id.styleView);
//        Button btn = (Button)findViewById(R.id.btn_style);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!isVisisble){
//                    isVisisble = true;
//                    //设置风格列表显示
//                    styleview.setVisibility(View.VISIBLE);
//                }else{
//                    isVisisble = false;
//                    //设置风格列表显示
//                    styleview.setVisibility(View.GONE);
//                }
//            }
//        });

        final Python py = Python.getInstance();
        mGallery = (LinearLayout) findViewById(R.id.id_gallery);
        for (int i = 0; i < mImgIds.length; i++) {
            final View view = mInflater.inflate(R.layout.activity_index_gallery,
                    mGallery, false);
            final ImageView img = (ImageView) view
                    .findViewById(R.id.id_index_gallery_item_image);
            img.setImageResource(mImgIds[i]);
            final TextView txt = (TextView) view
                    .findViewById(R.id.id_index_gallery_item_text);
            txt.setText(mImgNames[i]);

            //每张图片设置点击的响应
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isClick){
                        isClick = false;
                        img.setBackgroundColor(Color.parseColor("#ffffff"));

                    }else{
                        isClick = true;
                        img.setBackgroundColor(Color.parseColor("#ff0000"));
                        //如果数据为照片
                        if(getIntent().getType().equals("image/*")){
                            PyObject obj1 = py.getModule("py_stylization")
                                    .callAttr("style_transfer",uri,(txt.getText().toString()+".t7"));
                            Toast.makeText(getBaseContext(),"成功调用",Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });
            mGallery.addView(view);
        }

    }
}
