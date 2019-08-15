package com.yunbiao.yb_smart_passage.activity.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.Event.AdsOpenTimeEvent;
import com.yunbiao.yb_smart_passage.activity.Event.AdsUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.Event.AdsAirInfoEvent;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.bean.AdvertBean;
import com.yunbiao.yb_smart_passage.business.AirQualityUtil;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.ThreadUitls;
import com.yunbiao.yb_smart_passage.utils.xutil.MyXutils;
import com.yunbiao.yb_smart_passage.views.AdsSwitcher;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import okhttp3.Call;

public class ScreenSaveFragment extends Fragment implements AdsListener {
    private static final String TAG = "ScreenSaveFragment";
    private PropertyValuesHolder animY;

    private long AUTO_SCREEN_TIME = 10;
    private ObjectAnimator objectAnimator;

    private TextView tvTemp;
    private TextView tvWetness;
    private TextView tvVoc;
    private TextView tvPm25;
    private TextView tvHcho;
    private TextView tvCo2;
    private View root;

    private AdsSwitcher adsSwitcher;
    private boolean isInit = false;
    private String cacheAds;
    private View progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        root = inflater.inflate(R.layout.fragment_ads_h, container, false);
//        root = rootView.findViewById(R.id.rl_root);
        tvTemp = root.findViewById(R.id.tv_screen_temperature);
        tvCo2 = root.findViewById(R.id.tv_screen_co2);
        tvHcho = root.findViewById(R.id.tv_screen_hcho);
        tvPm25 = root.findViewById(R.id.tv_screen_pm2_5);
        tvVoc = root.findViewById(R.id.tv_screen_voc);
        tvWetness = root.findViewById(R.id.tv_screen_wetness);

        adsSwitcher = root.findViewById(R.id.ads_switcher);
        progressBar = root.findViewById(R.id.ll_progress);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAds();
            }
        });

        //先关闭广告
        closeAds();

        //初始化广告数据
        initAdsData();

        AirQualityUtil.getAirQuality(callback);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsAirInfoEvent airInfoEvent) {
        d("update: ----- 收到气候信息更新事件");
        AirQualityUtil.getAirQuality(callback);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsUpdateEvent updateEvent) {
        d("update: ----- 收到广告更新事件");
        isInit = true;
        getAdsData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(AdsOpenTimeEvent updateEvent) {
        d("update: ----- 收到更新广告时间的事件 " + updateEvent.getTime());
        if(updateEvent != null && updateEvent.getTime() > 0){
            AUTO_SCREEN_TIME = updateEvent.getTime();
        }
    }

    private void initAdsData() {
        ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                d("开始加载本地缓存广告...");
                cacheAds = SpUtils.getStr(SpUtils.AD_HENG);
                if(!TextUtils.isEmpty(cacheAds)){
                    AdvertBean advertBean = new Gson().fromJson(cacheAds, AdvertBean.class);
                    if(advertBean != null){
                        checkAds(advertBean.getAdvertObject());
                    }
                }
                getAdsData();
            }
        });
    }

    private void getAdsData() {
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        int companyid = SpUtils.getInt(SpUtils.COMPANY_ID);
        final Map<String, String> map = new HashMap<String, String>();
        map.put("comId", companyid + "");
        OkHttpUtils.post().url(ResourceUpdate.GETAD).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                d("请求失败..." + (e == null ? "NULL" : e.getMessage()));
            }

            @Override
            public void onAfter(int id) {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(String response, int id) {
                d("请求成功..." + response);
                AdvertBean advertBean = new Gson().fromJson(response, AdvertBean.class);
                if (advertBean == null) {
                    return;
                }

                if(!isInit && TextUtils.equals(cacheAds,response)){
                    d("数据未变动，不继续");
                    return;
                }

                d("缓存... ");
                SpUtils.saveStr(SpUtils.AD_HENG, response);

                if(advertBean.getStatus() != 1){
                    if(advertBean.getStatus() == 3){
                        //为3表示没广告，隐藏背景
                        adsSwitcher.clearData();
                    }
                    return;
                }
                checkAds(advertBean.getAdvertObject());
            }
        });
    }

    @Override
    public void detectFace() {
        if (root.isShown()) {
            closeAds();
        }
        restartAutoScreen();
    }

    private void checkAds(AdvertBean.AdvertObjectEntity objEntity) {
        if(objEntity == null){
            d("没有数据... ");
            return;
        }
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        int advertTime = objEntity.getAdvertTime();
        adsSwitcher.setSwitchTime(advertTime);

        List<AdvertBean.AdvertObjectEntity.Entity> imgArray = objEntity.getImgArray();
        List<AdvertBean.AdvertObjectEntity.Entity> videoArray = objEntity.getVideoArray();
        Queue<String> urlQueue = getUrlQueue(imgArray, videoArray);

        d("检查广告... ");

        final List<File> mAdsList = new ArrayList<>();
        download(urlQueue, new AdsCallback() {
            @Override
            public void getAds(File file) {
                d("getAds: ---------- 下载成功：" + file.getPath());
                mAdsList.add(file);
            }

            @Override
            public void finish() {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                d("finish: ---------- 结束");
                //没有广告数据则清除当前的广告内容并显示背景
                if(mAdsList.size() <= 0){
                    adsSwitcher.clearData();
                    return;
                }

                //有广告则隐藏背景
                adsSwitcher.setData(mAdsList);
            }
        });
    }

    private Queue<String> getUrlQueue(List<AdvertBean.AdvertObjectEntity.Entity> imgArray, List<AdvertBean.AdvertObjectEntity.Entity> videoArray){
        Queue<String> urlQueue = new LinkedList<>();
        if (imgArray != null) {
            for (AdvertBean.AdvertObjectEntity.Entity imgArrayEntity : imgArray) {
                String adUrl = imgArrayEntity.getAdvertimg();
                urlQueue.add(adUrl);
            }
        }

        if (videoArray != null) {
            for (AdvertBean.AdvertObjectEntity.Entity entity : videoArray) {
                String adUrl = entity.getAdvertimg();
                urlQueue.add(adUrl);
            }
        }
        return urlQueue;
    }

    private void download(final Queue<String> fileQueue, final AdsCallback callback) {
        if (fileQueue.size() <= 0) {
            if (callback != null) {
                callback.finish();
            }
            return;
        }
        final String adUrl = fileQueue.poll();
        String adPath = Constants.ADS_PATH + (adUrl.substring(adUrl.lastIndexOf("/") + 1));
        final File file = new File(adPath);
        d("检查广告文件..." + adPath);

        if (file.exists() && file.isFile()) {
            d("广告文件存在..." + file.getPath());
            if (callback != null) {
                callback.getAds(file);
            }
            download(fileQueue, callback);
            return;
        }

        d("广告文件不存在，准备下载..." + adUrl);
        MyXutils.getInstance().downLoadFile(adUrl, adPath, true, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                d("百分比---> " + ((float) current / total * 100));
            }

            @Override
            public void onSuccess(File result) {
                d("下载成功: " + result.getName());
                if (callback != null) {
                    callback.getAds(file);
                }
            }

            @Override
            public void onError(Throwable ex) {
                d("下载失败：" + adUrl);
            }

            @Override
            public void onFinished() {
                download(fileQueue, callback);
            }
        });
    }

    /*
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * =================================================================================================
    * */
    private void startAutoScreen() {
        root.removeCallbacks(autoScreenRunnable);
        root.postDelayed(autoScreenRunnable, AUTO_SCREEN_TIME * 1000);
    }

    private void restartAutoScreen() {
        startAutoScreen();
    }

    private void stopAutoScreen() {
        root.removeCallbacks(autoScreenRunnable);
    }

    private Runnable autoScreenRunnable = new Runnable() {
        @Override
        public void run() {
            if (root.isShown()) {
                return;
            }
            openAds();
        }
    };

    private void openAds() {
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return;
        }
        root.setVisibility(View.VISIBLE);
        startAnim(root, -root.getHeight(), 0, new Runnable() {
            @Override
            public void run() {
                stopAutoScreen();
                //开启广告以后开始计时
                adsSwitcher.justStartAutoPlay();
            }
        });
    }

    private void closeAds() {
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return;
        }
        startAnim(root, 0, -root.getHeight(), new Runnable() {
            @Override
            public void run() {
                startAutoScreen();
                root.setVisibility(View.GONE);
                //关闭广告后停止广告自动
                adsSwitcher.stopAutoPlay();
            }
        });
    }

    private void startAnim(final View view, int formY, int toY, final Runnable runnable) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);//开始动画前开启硬件加速
        animY = PropertyValuesHolder.ofFloat("translationY", formY, toY);//生成值动画
        //加载动画Holder
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, animY);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setLayerType(View.LAYER_TYPE_NONE, null);//动画结束时关闭硬件加速
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        objectAnimator.start();
    }

    private AirQualityUtil.AirQualityCallback callback = new AirQualityUtil.AirQualityCallback() {
        @Override
        public void onCallback(final AirQualityUtil.AirQualityBean.SingleAirBean airBean) {
            if (airBean == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String temperature = airBean.getTEMPERATURE();
                    String wetness = airBean.getWETNESS();
                    String pm25 = airBean.getPM25();
                    String co2 = airBean.getCO2();
                    String tvoc = airBean.getTVOC();
                    String hcho = airBean.getHCHO();

                    if (!TextUtils.isEmpty(temperature)) tvTemp.setText(temperature+"℃");
                    if (!TextUtils.isEmpty(wetness)) tvWetness.setText(wetness + "%");
                    if (!TextUtils.isEmpty(pm25)) tvPm25.setText(pm25);
                    if (!TextUtils.isEmpty(co2)) tvCo2.setText(co2);
                    if (!TextUtils.isEmpty(tvoc)) tvVoc.setText(tvoc);
                    if (!TextUtils.isEmpty(hcho)) tvHcho.setText(hcho);
                }
            });
        }
    };

    interface AdsCallback {
        void getAds(File file);

        void finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: -----------------");
        //返回页面后开始计时
        startAutoScreen();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ----------------- ");
        //页面被遮挡时隐藏界面停止
        root.setVisibility(View.GONE);
        //关闭广告后停止广告自动
        adsSwitcher.stopAutoPlay();
        stopAutoScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void d(@NonNull String msg) {
        Log.d(TAG, msg);
    }

}
