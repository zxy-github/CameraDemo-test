package com.example.lenovo.camerademo;
import android.content.SharedPreferences;
import android.hardware.Camera;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

/**
 * Created by LENOVO on 2020/3/6.
 */

public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    static Camera mCamera;
    static Camera.Parameters mParameters;
    //静态成员变量KEY_PREF_PREV_SIZE存储预览分辨率的key
    public static final String KEY_PREF_PREV_SIZE = "preview_size";
    //全部动态加载项
    public static final String KEY_PREF_PIC_SIZE = "picture_size";
    public static final String KEY_PREF_VIDEO_SIZE = "video_size";
    public static final String KEY_PREF_FLASH_MODE = "flash_mode";
    public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
    public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
    public static final String KEY_PREF_SCENE_MODE = "scene_mode";
    public static final String KEY_PREF_GPS_DATA = "gps_data";
    public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
    public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //是加载来自preferences.xml文件中的菜单条目
        addPreferencesFromResource(R.xml.preferences);

        //字体设置为白色
        getActivity().setTheme(R.style.PreferenceTheme);

        //在onCreate()触发时就调用这些load进行动态加载
        loadSupportedPreviewSize();
        loadSupportedPictureSize();
        loadSupportedVideoeSize();
        loadSupportedFlashMode();
        loadSupportedFocusMode();
        loadSupportedWhiteBalance();
        loadSupportedSceneMode();
        loadSupportedExposeCompensation();

        //显示偏好选项
        initSummary(getPreferenceScreen());
    }

    //onSharedPreferenceChanged()为监听事件回调
    //监听条目value变化就好了，一旦出现变化，就根据其key立即应用新的value到相机。
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //每次条目改变，更新显示
        updatePrefSummary(findPreference(key));
        switch (key) {
            case KEY_PREF_PREV_SIZE:
                setPreviewSize(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_PIC_SIZE:
                setPictureSize(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_FOCUS_MODE:
                setFocusMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_FLASH_MODE:
                setFlashMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_WHITE_BALANCE:
                setWhiteBalance(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_SCENE_MODE:
                setSceneMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_EXPOS_COMP:
                setExposComp(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_JPEG_QUALITY:
                setJpegQuality(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_GPS_DATA:
                setGpsData(sharedPreferences.getBoolean(key, false));
                break;
        }
        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //passCamera()用来将相机传输给SettingsFragment，SettingsFragment将相机保存到静态成员变量mCamera中
    public static void passCamera(Camera camera) {
        mCamera = camera;
        //getParameters()则用来获取相机参数
        mParameters = camera.getParameters();
    }

    //获取相机支持的所有预览分辨率，保存在List<Camera.Size>中
    private void loadSupportedPreviewSize() {
        cameraSizeListToListPreference(mParameters.getSupportedPreviewSizes(), KEY_PREF_PREV_SIZE);
    }

    //获取相机支持的所有照片分辨率，保存在List<Camera.Size>中
    private void loadSupportedPictureSize() {
        cameraSizeListToListPreference(mParameters.getSupportedPictureSizes(), KEY_PREF_PIC_SIZE);
    }

    //获取相机支持的所有视频分辨率，保存在List<Camera.Size>中
    private void loadSupportedVideoeSize() {
        cameraSizeListToListPreference(mParameters.getSupportedVideoSizes(), KEY_PREF_VIDEO_SIZE);
    }

    //获取相机支持的所有闪光灯模式，保存在List<Camera.Size>中
    private void loadSupportedFlashMode() {
        stringListToListPreference(mParameters.getSupportedFlashModes(), KEY_PREF_FLASH_MODE);
    }

    //获取相机支持的所有对焦模式，保存在List<Camera.Size>中
    private void loadSupportedFocusMode() {
        stringListToListPreference(mParameters.getSupportedFocusModes(), KEY_PREF_FOCUS_MODE);
    }

    //获取相机支持的所有白衡光，保存在List<Camera.Size>中
    private void loadSupportedWhiteBalance() {
        stringListToListPreference(mParameters.getSupportedWhiteBalance(), KEY_PREF_WHITE_BALANCE);
    }

    //获取相机支持的所有场景，保存在List<Camera.Size>中
    private void loadSupportedSceneMode() {
        stringListToListPreference(mParameters.getSupportedSceneModes(), KEY_PREF_SCENE_MODE);
    }

    //获取相机支持的所有曝光补偿，保存在List<Camera.Size>中
    private void loadSupportedExposeCompensation() {
        int minExposComp = mParameters.getMinExposureCompensation();
        int maxExposComp = mParameters.getMaxExposureCompensation();
        List<String> exposComp = new ArrayList<>();
        for (int value = minExposComp; value <= maxExposComp; value++) {
            exposComp.add(Integer.toString(value));
        }
        stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP);
    }

    //将List<Camera.Size>转换为List<String>
    private void cameraSizeListToListPreference(List<Camera.Size> list, String key) {
        List<String> stringList = new ArrayList<>();
        for (Camera.Size size : list) {
            String stringSize = size.width + "x" + size.height;
            stringList.add(stringSize);
        }
        stringListToListPreference(stringList, key);
    }

    //首先将List<String>转换为CharSequence[]；
    // 由getPreferenceScreen().findPreference()获取由key指定的菜单条目
    private void stringListToListPreference(List<String> list, String key) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);

        //由setEntries()和setEntryValues向这个菜单条目指定用户可见的所有value和代码可见的所有value
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

    /* 通过调用setDefault()，来添加动态默认值 */
    public static void setDefault(SharedPreferences sharedPrefs) {
        String valPreviewSize = sharedPrefs.getString(KEY_PREF_PREV_SIZE, null);
        //如果值为空则指定默认值，否则返回
        if (valPreviewSize == null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
            editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
            editor.putString(KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
            editor.putString(KEY_PREF_FOCUS_MODE, getDefaultFocusMode());
            editor.apply();
        }
    }
    /* 获取到相机的默认值，然后构造成为偏好设置中的格式 */
    private static String getDefaultPreviewSize() {
        Camera.Size previewSize = mParameters.getPreviewSize();
        return previewSize.width + "x" + previewSize.height;
    }

    private static String getDefaultPictureSize() {
        Camera.Size pictureSize = mParameters.getPictureSize();
        return pictureSize.width + "x" + pictureSize.height;
    }

    private static String getDefaultVideoSize() {
        Camera.Size VideoSize = mParameters.getPreferredPreviewSizeForVideo();
        return VideoSize.width + "x" + VideoSize.height;
    }

    private static String getDefaultFocusMode() {
        List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
        if (supportedFocusModes.contains("continuous-picture")) {
            return "continuous-picture";
        }
        return "continuous-video";
    }

    public static void init(SharedPreferences sharedPref) {
        /*以下set分别修改相机参数mParameters的不同属性*/
        setPreviewSize(sharedPref.getString(KEY_PREF_PREV_SIZE, ""));
        setPictureSize(sharedPref.getString(KEY_PREF_PIC_SIZE, ""));
        setFlashMode(sharedPref.getString(KEY_PREF_FLASH_MODE, ""));
        setFocusMode(sharedPref.getString(KEY_PREF_FOCUS_MODE, ""));
        setWhiteBalance(sharedPref.getString(KEY_PREF_WHITE_BALANCE, ""));
        setSceneMode(sharedPref.getString(KEY_PREF_SCENE_MODE, ""));
        setExposComp(sharedPref.getString(KEY_PREF_EXPOS_COMP, ""));
        setJpegQuality(sharedPref.getString(KEY_PREF_JPEG_QUALITY, ""));
        setGpsData(sharedPref.getBoolean(KEY_PREF_GPS_DATA, false));
        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    private static void setPreviewSize(String value) {
        String[] split = value.split("x");
        mParameters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setPictureSize(String value) {
        String[] split = value.split("x");
        mParameters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setFocusMode(String value) {
        mParameters.setFocusMode(value);
    }

    private static void setFlashMode(String value) {
        mParameters.setFlashMode(value);
    }

    private static void setWhiteBalance(String value) {
        mParameters.setWhiteBalance(value);
    }

    private static void setSceneMode(String value) {
        mParameters.setSceneMode(value);
    }

    private static void setExposComp(String value) {
        mParameters.setExposureCompensation(Integer.parseInt(value));
    }

    private static void setJpegQuality(String value) {
        mParameters.setJpegQuality(Integer.parseInt(value));
    }

    private static void setGpsData(Boolean value) {
        if (value.equals(false)) {
            mParameters.removeGpsData();
        }
    }

    //让ListPreference显示当前值
    private static void initSummary(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
                initSummary(prefGroup.getPreference(i));
            }
        } else {
            updatePrefSummary(pref);
        }
    }
    //处理具体的ListPreference，将其用户可见的值getEntry()通过setSummary()赋给summary
    private static void updatePrefSummary(Preference pref) {
        if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getEntry());
        }
    }
}
