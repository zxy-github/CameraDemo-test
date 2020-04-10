package com.example.lenovo.camerademo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by LENOVO on 2020/3/3.
 */

//Surface用来处理直接呈现在屏幕上的内容
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = "CameraPreview";
    //定义surface持有者
    //只有mHolder才能对对应的Surface进行修改
    private SurfaceHolder mHolder;
    //定义mCamera保存相机Camera的实例
    private Camera mCamera;

//    //设置前后置相机标志
//    private int iBackCameraIndex  = 0;
//    private int iFontCameraIndex  = 1;
//    private int currentCameraIndex = 0;

    /* 设置生成文件名 */
    //1,2用来标记文件为照片还是视频
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;

    //录像交给MediaRecorder类在做
    private MediaRecorder mMediaRecorder;

    public CameraPreview(Context context){
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    //相机初始化，打开相机
    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                Log.d(TAG, "camera is not available");
            }
        }
        return mCamera;
    }
//    //切换摄像头
//
//    public void onChange() {
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//        }
//        if (currentCameraIndex==iBackCameraIndex){
//            currentCameraIndex = iFontCameraIndex;
//        }else{
//            currentCameraIndex = iBackCameraIndex;
//        }
//
//        mCamera = Camera.open(currentCameraIndex);
//        try {
//            mCamera.setPreviewDisplay(mHolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mCamera.startPreview();
//    }

    //surface生成
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //getCameraInstance()是一个比较安全的获取并打开相机的方法
        mCamera = getCameraInstance();
        try{
            //setPreviewDisplay()方法就是告知将预览帧数据交给谁，
            // 这里当然就是这个Surface的Holder
            mCamera.setPreviewDisplay(holder);
            //用startPreview()开启相机
            mCamera.startPreview();
        }catch (IOException e){
            Log.d(TAG,"Error setting camera preview" + e.getMessage());
        }
    }

    //surface内容生成
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 设置预览角度
        int rotation = getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);

        //拍照角度
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);

//        //应用新的尺寸
//        adjustDisplayRatio(rotation);
    }

    //surface销毁 相机是共享资源，在APP运行结束后就应当“放弃”相机
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //销毁过程就是构造函数和surfaceCreated()的逆过程。
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    //根据参数中指定的文件类型，生成File类型的实例，供调用者写入文件
    private File getOutputMediaFile(int type) {
        //获取SD卡中相机保存的位置
        File appDir = new File(Environment.getExternalStorageDirectory(),"/DCIM/Camera");
        //如果目录不存在需要创建目录
        if(!appDir.exists()){
            appDir.mkdir();
            Toast.makeText(getContext(),"文件夹创建成功",Toast.LENGTH_LONG).show();
        }

        String fileName;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            fileName = "IMG_" + timeStamp + ".jpg";
            outputMediaFileType = "image/*";
        } else if (type == MEDIA_TYPE_VIDEO) {
            fileName = "VID_" + timeStamp + ".mp4";
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        mediaFile = new File(appDir,fileName);
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType() {
        return outputMediaFileType;
    }


    //拍照
    public void takePicture(final ImageView view) {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
            //拍照成功提示框
            Toast.makeText(getContext(), "拍照成功", Toast.LENGTH_SHORT).show();

            //创建文件
            File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            //根据图片创建位图对象
            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                //将图片压缩成jpeg格式输出到输出流中
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.flush();       //将缓冲区的数据全部写到输出流中
                fos.close();    //关闭文件输出流对象

                //设置预览框路径
                view.setImageURI(outputMediaFileUri);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();;
            }

            //将照片插入到系统图库
            try{
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                        file.getAbsolutePath(),file.getName(),null);
            }catch (IOException e){
                e.printStackTrace();
            }

            //最后通知图库更新
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://"+"")));

            Toast.makeText(getContext(),"照片保存至：" + file,Toast.LENGTH_LONG).show();

            }
        });
    }

    //重新开始预览
    public void resetCamera(){
        mCamera.startPreview();
    }

    //录像
//    public boolean startRecording() {
//        if (prepareVideoRecorder()) {
//            //提示开始录制
//            Toast.makeText(getContext(),"开始录制",Toast.LENGTH_SHORT).show();
//            //开始录像
//            mMediaRecorder.start();
//            return true;
//        } else {
//            //释放MediaRecorder
//            releaseMediaRecorder();
//        }
//        return false;
//    }

    public void stopRecording(final ImageView view) {
        if (mMediaRecorder != null) {
            //停止录像
            mMediaRecorder.stop();
            //提示结束录制
            Toast.makeText(getContext(),"录制结束",Toast.LENGTH_SHORT).show();
//            根据指定的视频路径，生成了一张视频预览图
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            //将这个图片交给ImageView显示
            view.setImageBitmap(thumbnail);
        }
        releaseMediaRecorder();
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

//    //用来判断当前是否正在录像。
//    public boolean isRecording() {
//        return mMediaRecorder != null;
//    }

    // 实例化MediaRecorder为成员变量mMediaRecorder
    // 指定相机、音频源、视频源、录制视频参数、输出文件路径以及预览等
    public void startRecording() {
        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        //解锁摄像头
        mCamera.unlock();
        //指定摄像头对象
        mMediaRecorder.setCamera(mCamera);

//        //判断相机是否为空
//        if (mCamera != null) {
//            mCamera.stopPreview();
//            mCamera.unlock();
//            mMediaRecorder.setCamera(mCamera);
//        }

//        重置MediaRecorder对象
//        mMediaRecorder.reset();

        //获取音频
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        //获取视频
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //指定视频输出格式为mp4
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //指定声音编码格式
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //指定视频编码格式
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

//        从Preference中读取视频分辨率偏好设置，并将其应用到mMediaRecorder
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefVideoSize = prefs.getString("video_size", "");
        String[] split = prefVideoSize.split("x");
        mMediaRecorder.setVideoSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).getAbsolutePath());

        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        } catch(IOException e){
            e.printStackTrace();
        }

        //开始录像
        mMediaRecorder.start();
        //提示开始录制
        Toast.makeText(getContext(),"开始录制",Toast.LENGTH_SHORT).show();
    }

    //获取相机需要旋转的角度
    public int getDisplayOrientation() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;

            layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

}
