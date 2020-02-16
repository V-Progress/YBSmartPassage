package com.yunbiao.yb_smart_passage.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.yb_smart_passage.activity.Event.VisitorUpdateEvent;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.bean.VisitorResponse;
import com.yunbiao.yb_smart_passage.db2.DaoManager;
import com.yunbiao.yb_smart_passage.db2.VisitorBean;
import com.yunbiao.yb_smart_passage.faceview.FaceSDK;
import com.yunbiao.yb_smart_passage.system.HeartBeatClient;
import com.yunbiao.yb_smart_passage.utils.xutil.MyXutils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

public class VisitorManager {
    private static final String TAG = "VisitorManager";
    private static VisitorManager visitorManager = new VisitorManager();
    private final ScheduledExecutorService scheduledExecutorService;

    public static VisitorManager instance() {
        return visitorManager;
    }

    private VisitorManager() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(autoSyncRunnable, 5, TimeUnit.MINUTES);
    }

    private Runnable autoSyncRunnable = new Runnable() {
        @Override
        public void run() {
            syncVisitor(new Runnable() {
                @Override
                public void run() {
                    scheduledExecutorService.schedule(autoSyncRunnable, 10, TimeUnit.MINUTES);
                }
            });
        }
    };

    public synchronized void syncVisitor(final Runnable finishRunnable) {
        OkHttpUtils.post()
                .url(ResourceUpdate.GET_VISITOR)
                .addParams("deviceNo", HeartBeatClient.getDeviceNo())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + (e == null ? "NULL" : e.getMessage()));
                finishRunnable.run();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);

                if (TextUtils.isEmpty(response)) {
                    finishRunnable.run();
                    return;
                }

                VisitorResponse visitorResponse = new Gson().fromJson(response, VisitorResponse.class);
                if (visitorResponse.getStatus() != 1) {
                    finishRunnable.run();
                    return;
                }

                d("生成远程数据");
                Map<Long, VisitorBean> remoteDatas = new HashMap<>();
                List<VisitorBean> visitor = visitorResponse.getVisitor();
                for (VisitorBean visitorBean : visitor) {
                    remoteDatas.put(visitorBean.getId(), visitorBean);
                }

                //获取本地数据
                List<VisitorBean> localDatas = DaoManager.get().queryAll(VisitorBean.class);
                int localNumber = localDatas == null ? 0 : localDatas.size();

                d("检查已删除数据");
                if (localDatas != null) {
                    for (VisitorBean localData : localDatas) {
                        long id1 = localData.getId();
                        if (!remoteDatas.containsKey(id1)) {
                            FaceSDK.instance().removeUser(String.valueOf(id1));
                            DaoManager.get().delete(localData);
                            d("删除数据：" + localData.getName());
                        }
                    }
                }

                d("更新访客信息");
                for (Map.Entry<Long, VisitorBean> entry : remoteDatas.entrySet()) {
                    VisitorBean value = entry.getValue();
                    value.setFaceId(value.getFaceId());
                    String head = value.getHead();
                    String filepath = Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
                    value.setHeadPath(filepath);
                    long l = DaoManager.get().addOrUpdate(value);
                    d("更新数据结果：" + l);
                }

                List<VisitorBean> visitorBeans = DaoManager.get().queryAll(VisitorBean.class);
                for (VisitorBean visitorBean : visitorBeans) {
                    Log.e(TAG, "onResponse: ----- " + visitorBean.toString());
                }

                d("检查头像");
                Queue<VisitorBean> updateList = new LinkedList<>();
                //检查头像
                localDatas = DaoManager.get().queryAll(VisitorBean.class);
                for (VisitorBean localData : localDatas) {
                    String headPath = localData.getHeadPath();
                    File file = new File(headPath);
                    if (!file.exists()) {
                        updateList.add(localData);
                    }
                }

                d("下载头像");
                downloadHead(updateList, new Runnable() {
                    @Override
                    public void run() {
                        checkFaceDB(finishRunnable);
                    }
                });
            }
        });
    }

    private void checkFaceDB(Runnable finishRunnable) {
        d("检查人脸库");
        List<VisitorBean> visitorBeans = DaoManager.get().queryAll(VisitorBean.class);
        Map<String, FaceUser> allFaceData = FaceSDK.instance().getAllFaceData();

        d("对比数据：" + visitorBeans.size() + " : " + allFaceData.size());
        if (visitorBeans != null) {
            //循环检查库中是否有该访客
            for (VisitorBean visitorBean : visitorBeans) {
                String faceId = visitorBean.getFaceId();
                String headPath = visitorBean.getHeadPath();
                if (TextUtils.isEmpty(headPath) || !new File(headPath).exists()) {
                    d("头像不存在，跳过：" + visitorBean.getName());
                    continue;
                }

                //如果有该访客则检查头像路径，如果不相同则更新
                if (allFaceData.containsKey(faceId)) {
                    FaceUser faceUser = allFaceData.get(faceId);
                    String imagePath = faceUser.getImagePath();
                    if (!TextUtils.equals(imagePath, headPath)) {
                        faceUser.setImagePath(headPath);
                        FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                            @Override
                            public void onUserResult(boolean b, int i) {
                                d("更新访客结果：" + (b + " —— " + i));
                            }
                        });
                    }
                    //如果没有该访客则添加
                } else {
                    FaceSDK.instance().addUser(faceId, headPath, new FaceUserManager.FaceUserCallback() {
                        @Override
                        public void onUserResult(boolean b, int i) {
                            d("添加访客结果：" + (b + " —— " + i));
                        }
                    });
                }
            }
        }

        EventBus.getDefault().postSticky(new VisitorUpdateEvent());
        finishRunnable.run();
    }

    private void downloadHead(final Queue<VisitorBean> queue, final Runnable runnable) {
        d("开始下载");
        if (queue == null || queue.size() <= 0) {
            d("下载结束");
            runnable.run();
            return;
        }

        final VisitorBean visitorBean = queue.poll();
        d("下载：" + visitorBean.getName() + " —— " + visitorBean.getHead());
        if (!TextUtils.isEmpty(visitorBean.getHeadPath()) && new File(visitorBean.getHeadPath()).exists()) {
            d("本地头像地址为空");
            downloadHead(queue, runnable);
            return;
        }

        String headUrl = visitorBean.getHead();
        MyXutils.getInstance().downLoadFile(headUrl, visitorBean.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            int downloadTag = 0;

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "下载进度... " + current + " —— " + total);
            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG, "下载成功... " + result.getPath());
                downloadTag = 0;
                visitorBean.setHeadPath(result.getPath());
            }

            @Override
            public void onError(Throwable ex) {
                Log.e(TAG, "下载失败... " + (ex != null ? ex.getMessage() : "NULL"));
                downloadTag = -1;
            }

            @Override
            public void onFinished() {
                visitorBean.setAddTag(downloadTag);
                long l = DaoManager.get().addOrUpdate(visitorBean);
                d("更新数据结果... " + l);
                downloadHead(queue, runnable);
            }
        });
    }


    private void d(String log) {
        Log.d(TAG, log);
    }
}
