package com.yunbiao.yb_smart_passage.activity.fragment;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_passage.activity.Event.IntroduceUpdateEvent;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.bean.IntroduceBean;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

public class ResourceResolver{
    private static final String TAG = "ResourceResolver";
    private List<IntroBean> introList;
    private final ExecutorService executorService;
    private String cacheData;
    private List<IntroduceBean.Propa> propaArray;

    private static ResourceResolver instance = new ResourceResolver();

    public static ResourceResolver instance(){
        return instance;
    }

    private void d(String msg){
        Log.d(TAG, msg);
    }

    private ResourceResolver() {
        cacheData = SpUtils.getStr(SpUtils.COMPANY_INTRO);
        executorService = Executors.newFixedThreadPool(2);
    }

    public void init(){
        loadIntroduce();
    }

    private void loadIntroduce(){
        int companyId = SpUtils.getInt(SpUtils.COMPANY_ID);
        d(ResourceUpdate.GET_INTRODUCE + " -- " + companyId + "");
        OkHttpUtils.post()
                .url(ResourceUpdate.GET_INTRODUCE)
                .addParams("comId", companyId + "")
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        d("onError---" + (e == null ? "NULL" : e.getMessage()));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        cacheData = response;
                    }

                    @Override
                    public void onAfter(int id) {
                        if(TextUtils.isEmpty(cacheData)){
                            EventBus.getDefault().postSticky(new IntroduceUpdateEvent(false));
                            if(mListener != null){
                                mListener.noResource();
                            }
                            return;
                        }

                        SpUtils.saveStr(SpUtils.COMPANY_INTRO,cacheData);
                        IntroduceBean introduceBean = new Gson().fromJson(cacheData, IntroduceBean.class);
                        propaArray = introduceBean.getPropaArray();
                        if (introduceBean.getStatus() != 1 || propaArray == null || propaArray.size() <= 0) {
                            EventBus.getDefault().postSticky(new IntroduceUpdateEvent(false));
                            if(mListener != null){
                                mListener.noResource();
                            }
                            return;
                        }

                        EventBus.getDefault().postSticky(new IntroduceUpdateEvent(true));
                        if(mListener != null){
                            mListener.onResolveBefore();
                        }

                        resolve();
                    }

                });
    }

    private DownloadListener mListener;

    public void setListener(DownloadListener mListener) {
        this.mListener = mListener;
    }

    private void resolve(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                introList = getIntroList(propaArray);
                if(introList == null) {
                    d("没有资源... ");
                    if(mListener != null){
                        mListener.noResource();
                    }
                    return;
                }

                for (IntroBean introBean : introList) {
                    d("检查资源=============== " + introBean.toString());
                }

                d("开始... ");
                Queue<IntroBean> introQueue = new LinkedList<>();
                introQueue.addAll(introList);

                checkResource(introQueue, mListener);
            }
        });
    }

    private List<IntroBean> getIntroList(List<IntroduceBean.Propa> propaArray) {
        if (propaArray == null) {
            return null;
        }

        List<IntroBean> introList = new ArrayList<>();
        for (IntroduceBean.Propa propa : propaArray) {
            IntroBean introBean = new IntroBean();
            introBean.type = propa.getType();
            introBean.name = propa.getName();
            introBean.logo = new IntroBean.ResourceBean();
            String url = propa.getLogo();
            introBean.logo.setUrl(url);
            introBean.logo.setPath(getLocalPath(url));

            if (introBean.type != 1) {
                introBean.url = propa.getUrl();
                introList.add(introBean);
                continue;
            }

            introBean.setTime(propa.getTime());
            introBean.resList  = new ArrayList<>();

            List<String> imgArray = propa.getImgArray();
            if (imgArray != null) {
                for (String imgUrl : imgArray) {
                    IntroBean.ResourceBean resourceBean = new IntroBean.ResourceBean();
                    resourceBean.setUrl(imgUrl);
                    resourceBean.setPath(getLocalPath(imgUrl));
                    introBean.resList.add(resourceBean);
                }
            }
            List<String> videoArray = propa.getVideoArray();
            if (videoArray != null) {
                for (String videoUrl : videoArray) {
                    IntroBean.ResourceBean resourceBean = new IntroBean.ResourceBean();
                    resourceBean.setUrl(videoUrl);
                    resourceBean.setPath(getLocalPath(videoUrl));
                    introBean.resList.add(resourceBean);
                }
            }

            introList.add(introBean);
        }
        return introList;
    }

    private void e(String msg){
        Log.e(TAG, msg);
    }

    private void checkResource(final Queue<IntroBean> introQueue, final DownloadListener listener){
        if(introQueue == null || introQueue.size() <= 0){
            d("结束... ");
            if(listener != null){
                listener.onResolveFinish(introList);
            }
            return;
        }
        final IntroBean poll = introQueue.poll();
        e("检查资源... " + poll.toString());

        Queue<IntroBean.ResourceBean> resourceQueue = new LinkedList<>();
        resourceQueue.offer(poll.logo);

        if(poll.type == 1){
            List<IntroBean.ResourceBean> resList = poll.getResList();
            for (IntroBean.ResourceBean resourceBean : resList) {
                resourceQueue.offer(resourceBean);
            }
        }

        download(resourceQueue, new Runnable() {
            @Override
            public void run() {
                e("下载完毕... ");
                if(listener != null){
                    listener.onResolveSuccess(poll);
                }
                checkResource(introQueue,listener);
            }
        });
    }

    private String getLocalPath(String url){
        String name = url.substring(url.lastIndexOf("/") + 1);
        return Constants.ADS_PATH + name;
    }

    private void download(final Queue<IntroBean.ResourceBean> urlQueue, @NonNull final Runnable runnable){
        if(urlQueue == null || urlQueue.size() <= 0){
            d("---下载完毕---");
            runnable.run();
            return;
        }

        d("---开始下载---");

        final IntroBean.ResourceBean poll = urlQueue.poll();
        d("---下载地址---" + poll.url);
        d("---本地地址---" + poll.path);

        String path = poll.path;
        if(!TextUtils.isEmpty(path) && new File(path).exists()){
            d("---文件存在---");
            download(urlQueue,runnable);
            return;
        }

        d("---下载中---");
        String url = poll.url;
        MyXutils.getInstance().downLoadFile(url, path, false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                d("---进度--- " + current + " --- " + total);
            }

            @Override
            public void onSuccess(File result) {
                d("---成功---" + result.getPath());
                download(urlQueue,runnable);
            }

            @Override
            public void onError(Throwable ex) {
                d("---失败---" + (ex == null? " NULL " : ex.getMessage()));
                urlQueue.offer(poll);
                download(urlQueue,runnable);
            }

            @Override
            public void onFinished() {

            }
        });
    }

    public interface DownloadListener{
        void onResolveBefore();
        void onResolveSuccess(IntroBean introBean);
        void onResolveFinish(List<IntroBean> introList);
        void noResource();
    }

    public static class IntroBean{
        @Override
        public String toString() {
            return "IntroBean{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", url='" + url + '\'' +
                    ", logo=" + logo +
                    ", resList=" + resList +
                    '}';
        }

        String name;
        int type;
        String url;
        ResourceBean logo;
        int time;
        List<ResourceBean> resList;

        public ResourceBean getLogo() {
            return logo;
        }

        public void setLogo(ResourceBean logo) {
            this.logo = logo;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public static class ResourceBean{
            @Override
            public String toString() {
                return "ResourceBean{" +
                        "url='" + url + '\'' +
                        ", path='" + path + '\'' +
                        '}';
            }

            String url;
            String path;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ResourceBean> getResList() {
            return resList;
        }

        public void setResList(List<ResourceBean> resList) {
            this.resList = resList;
        }
    }
}