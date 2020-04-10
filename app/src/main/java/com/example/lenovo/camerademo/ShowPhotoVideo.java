package com.example.lenovo.camerademo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

/**
 * Created by LENOVO on 2020/3/11.
 */

public class ShowPhotoVideo extends Activity{
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //代码直接编写界面
        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        //指定路径
        Uri uri = getIntent().getData();

        //如果数据为照片
        if(getIntent().getType().equals("image/*")){
            ImageView view = new ImageView(this);
            view.setImageURI(uri);
            view.setLayoutParams(layoutParams);
            relativeLayout.addView(view);
        }
        //如果为视频
        else{
            MediaController mc = new MediaController(this);
            VideoView view = new VideoView(this);
            mc.setAnchorView(view);
            mc.setMediaPlayer(view);
            view.setMediaController(mc);
            view.setVideoURI(uri);
            view.start();
            view.setLayoutParams(layoutParams);
            relativeLayout.addView(view);
        }
        setContentView(relativeLayout, layoutParams);
    }

}
