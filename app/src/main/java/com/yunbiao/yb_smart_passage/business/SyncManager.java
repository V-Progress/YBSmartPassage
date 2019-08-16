package com.yunbiao.yb_smart_passage.business;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.user.FaceUserManager;
import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.activity.EmployListActivity;
import com.yunbiao.yb_smart_passage.activity.Event.AdsUpdateEvent;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.bean.CompanyBean;
import com.yunbiao.yb_smart_passage.bean.StaffBean;
import com.yunbiao.yb_smart_passage.db2.DaoManager;
import com.yunbiao.yb_smart_passage.db2.UserBean;
import com.yunbiao.yb_smart_passage.db2.VisitorBean;
import com.yunbiao.yb_smart_passage.faceview.FaceSDK;
import com.yunbiao.yb_smart_passage.system.HeartBeatClient;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.xutil.MyXutils;
import com.yunbiao.yb_smart_passage.views.FloatSyncView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2019/5/14.
 */

public class SyncManager extends BroadcastReceiver {

    private static SyncManager instance;
    private Activity mAct;
    private boolean isLocalServ = false;

    public static final int TYPE_ADD = 0;
    public static final int TYPE_UPDATE_HEAD = 2;

    private FloatSyncView floatSyncView;
    private ExecutorService executorService;

    private int mUpdateTotal = 0;//更新总数
    private int mCurrDownloadIndex = 0;//当前索引

    private long initOffset = 12 * 60 * 60 * 1000;//更新间隔时间12小时

    public static SyncManager instance() {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager();
                }
            }
        }
        return instance;
    }

    private SyncManager() {
        File file = new File(Constants.HEAD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
            long lastInitTime = SpUtils.getLong(SpUtils.LAST_INIT_TIME);
            long currTime = System.currentTimeMillis();
            Log.e(TAG, "onReceive: -----" + lastInitTime + " --- " + currTime);
            if (currTime - lastInitTime >= initOffset) {//如果大于间隔则同步
                initInfo();
            } else {//如果小于间隔则获取公司数据
                loadCompany(false);
            }
        }
    }

    public interface LoadListener {
        void onLoaded();

        void onLogoLoded();

        void onFinish();
    }

    private LoadListener mListener;

    /***
     * 初始化数据
     * @param act
     * @return
     */
    public SyncManager init(@NonNull Activity act, LoadListener listener) {
        mAct = act;
        mListener = listener;
        if(executorService == null){
            executorService = Executors.newFixedThreadPool(2);
        }

        String webBaseUrl = ResourceUpdate.WEB_BASE_URL;
        String[] split = webBaseUrl.split(":");
        for (String s : split) {
            if (s.startsWith("192.168")) {
                isLocalServ = true;
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mAct.registerReceiver(this, filter);

        if (mListener != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onLoaded();
                }
            });
        }
        return instance;
    }

    /***
     * 全部流程重新初始化
     */
    public void initInfo() {
        OkHttpUtils.getInstance().cancelTag(this);
        cancelTimer();
        mUpdateTotal = 0;
        mCurrDownloadIndex = 0;
        showUI();
        loadCompany(true);
    }

    private void saveCompanyInfo(int id, String name, String pwd) {
        SpUtils.saveInt(SpUtils.COMPANY_ID, id);
        SpUtils.saveStr(SpUtils.COMPANY_NAME, name);
        SpUtils.saveStr(SpUtils.MENU_PWD, pwd);
        if (mListener != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onLoaded();
                }
            });
        }
    }

    private void checkLogoFile(String logoUrl, String localPath) {
        if (TextUtils.isEmpty(logoUrl)) {
            return;
        }
        String urlName = null;
        if (TextUtils.isEmpty(logoUrl)) {
            urlName = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
        }

        String localName = null;
        if (!TextUtils.isEmpty(localPath)) {
            localName = localPath.substring(localPath.lastIndexOf("/") + 1);
        }

        if (TextUtils.isEmpty(urlName) || TextUtils.isEmpty(localName)) {
            return;
        }

        if (TextUtils.equals(urlName, localName)) {
            return;
        }

        //生成路径
        File logoFile = new File(Constants.DATA_PATH, "logo_" + localName);
        MyXutils.getInstance().downLoadFile(logoUrl, logoFile.getPath(), false, new MyXutils.XDownLoadCallBack() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                SpUtils.saveStr(SpUtils.COMPANY_LOGO, result.getPath());
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinished() {
                if (mListener != null) {
                    mAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onLogoLoded();
                        }
                    });
                }
            }
        });
    }

    public void loadCompany(final boolean isInit) {
        setInfo("获取公司信息");
        d("-------------" + ResourceUpdate.COMPANYINFO);
        final Map<String, String> map = new HashMap<>();
        String deviceNo = HeartBeatClient.getDeviceNo();
        Log.e(TAG, "loadCompany: " + deviceNo);
        map.put("deviceNo", deviceNo);
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.COMPANYINFO).build().execute(new MyStringCallback<CompanyBean>(MyStringCallback.STEP_COMPANY) {
            @Override
            public void onRetryAfter5s() {
                loadCompany(isInit);
            }

            @Override
            public void onSucc(final String response, final CompanyBean companyBean) {
                Log.e(TAG, "onSucc: " + companyBean.toString());
                int companyId = SpUtils.getInt(SpUtils.COMPANY_ID);
                String compaName = SpUtils.getStr(SpUtils.COMPANY_NAME);
                String menuPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                String logoPath = SpUtils.getStr(SpUtils.COMPANY_LOGO);

                int comid = companyBean.getCompany().getComid();
                String name = companyBean.getCompany().getComname();
                String pwd = companyBean.getCompany().getDevicePwd();
                String logoUrl = companyBean.getCompany().getComlogo();

                //保存公司信息
                if (companyId != comid || !TextUtils.equals(compaName, name) || !TextUtils.equals(menuPwd, pwd)) {
                    saveCompanyInfo(comid, name, pwd);
                }

                //检查logo文件
                checkLogoFile(logoUrl, logoPath);

                //判断公司是否改变
                if (isInit || (comid != companyId)) {
                    loadStaff(companyBean);

                    SpUtils.saveInt(SpUtils.COMPANY_ID, comid);

                    EventBus.getDefault().postSticky(new AdsUpdateEvent());
                }
            }

            @Override
            public void onFailed() {

            }
        });
    }

    //加载员工信息
    private void loadStaff(final CompanyBean companyBean) {
        setInfo("获取人员信息");
        int comId = companyBean.getCompany().getComid();
        if (comId == 0) {
            setErrInfo("数据异常");
            return;
        }
        final HashMap<String, String> map = new HashMap<>();
        map.put("companyId", comId + "");
        OkHttpUtils.post().params(map).tag(this).url(ResourceUpdate.GETSTAFF).build().execute(new MyStringCallback<StaffBean>(MyStringCallback.STEP_STAFF) {
            @Override
            public void onRetryAfter5s() {
                loadStaff(companyBean);
            }

            @Override
            public void onFailed() {
            }

            @Override
            public void onSucc(String response, StaffBean staffBean) {
                Log.e(TAG, "onSucc: " + response);

                Log.e(TAG, "onSucc: " + staffBean.toString());
                syncDao(staffBean);
            }
        });
    }

    //同步数据库
    private void syncDao(final StaffBean staffBean) {
        if (staffBean.getStatus() != 1) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeNotSave();
            return;
        }

        if(executorService == null){
            executorService = Executors.newFixedThreadPool(2);
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                setInfo("正在同步");
                syncUserDao(staffBean);
            }
        });
    }

    private void syncUserDao(final StaffBean staffBean) {
        d("开始同步员工数据");
        if (staffBean == null) {
            d("没有员工数据");
            setInfo("没有员工数据");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeNotSave();
            return;
        }

        List<StaffBean.DepEntry> dep = staffBean.getDep();
        if (dep == null) {
            setInfo("没有员工数据");
            d("没有员工数据");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeNotSave();
            return;
        }

        d("检查所有员工");
        //生成云端数据的所有员工
        Map<Long, UserBean> remoteDatas = new HashMap<>();
        for (StaffBean.DepEntry depEntry : dep) {
            int depId = depEntry.getDepId();
            String depName = depEntry.getDepName();

            List<UserBean> entry = depEntry.getEntry();
            if (entry == null) {
                continue;
            }

            for (UserBean userBean : entry) {
                userBean.setDepartId(depId);
                userBean.setDepartName(depName);
                String head = userBean.getHead();
                if (!TextUtils.isEmpty(head)) {
                    String filepath = Constants.HEAD_PATH + head.substring(head.lastIndexOf("/") + 1);
                    userBean.setHeadPath(filepath);
                }
                remoteDatas.put(userBean.getId(), userBean);
            }
        }

        //获取本地数据
        List<UserBean> localDatas = DaoManager.get().queryAll(UserBean.class);
        int localNumber = localDatas == null ? 0 : localDatas.size();
        d("数据：云端：" + remoteDatas.size() + "---本地：" + localNumber);

        d("检查删除员工");
        //检查已删除
        if (localDatas != null) {
            for (UserBean localData : localDatas) {
                long id = localData.getId();
                //如果远程数据不包含这个id，则本地库删除
                if (!remoteDatas.containsKey(id)) {
                    String faceId = localData.getFaceId();
                    FaceSDK.instance().removeUser(faceId);
                    DaoManager.get().delete(localData);
                    d("删除数据：" + id);
                }
            }
        }

        d("更新员工信息");
        //先更新信息
        for (Map.Entry<Long, UserBean> entry : remoteDatas.entrySet()) {
            UserBean remoteBean = entry.getValue();
            long l = DaoManager.get().addOrUpdate(remoteBean);
            d("更新结果：" + l);
        }

        d("检查头像数据");
        Queue<UserBean> updateList = new LinkedList<>();
        //检查头像
        localDatas = DaoManager.get().queryAll(UserBean.class);
        for (UserBean localData : localDatas) {
            String headPath = localData.getHeadPath();
            File file = new File(headPath);
            if(!file.exists()){
                updateList.add(localData);
            }
        }

        d("下载头像");
        setInfo("下载头像");
        mUpdateTotal = updateList.size();
        showProgress();
        downloadHead(updateList, new Runnable() {
            @Override
            public void run() {
                d("下载结束");
                mCurrDownloadIndex = 0;

                //检查人脸库
                checkFaceDB(new Runnable() {
                    @Override
                    public void run() {
                        d("检查员工库结束");
                        d("\n");
                        d("\n");

                        setInfo("检查访客库");
                        d("检查访客库");
                        //检查访客库
                        VisitorManager.instance().syncVisitor(new Runnable() {
                            @Override
                            public void run() {
                                d("检查访客库完毕");

                                d("全部结束");
                                close();
                            }
                        });
                    }
                });
            }
        });
    }

    private void checkFaceDB(Runnable runnable){
        setInfo("检查人脸库");
        d("检查人脸库");

        //取出数据库中的数据和人脸库中的数据并做对比
        final List<UserBean> userBeans = DaoManager.get().queryAll(UserBean.class);
        Map<String, FaceUser> allUserMap = FaceSDK.instance().getAllFaceData();

        d("对比人脸库和远程数据：" + userBeans.size() + " : " + allUserMap.size());

        if(userBeans == null || userBeans.size()<=0){
            FaceSDK.instance().removeAllUser(new FaceUserManager.FaceUserCallback() {
                @Override
                public void onUserResult(boolean b, int i) {
                    d("删除全部员工结果：" + b + " —— " + i);
                }
            });
        } else {
            for (int i = 0; i < userBeans.size(); i++) {
                UserBean userBean = userBeans.get(i);
                setTextProgress((i+1) + "/" + userBeans.size());
                String faceId = userBean.getFaceId();
                String headPath = userBean.getHeadPath();
                if(TextUtils.isEmpty(headPath) || !new File(headPath).exists()){
                    d("头像不存在，跳过：" + userBean.getName());
                    continue;
                }

                if(allUserMap.containsKey(faceId)){
                    FaceUser faceUser = allUserMap.get(faceId);
                    String imagePath = faceUser.getImagePath();
                    if(!TextUtils.equals(imagePath,headPath)){
                        faceUser.setImagePath(headPath);
                        FaceSDK.instance().update(faceUser, new FaceUserManager.FaceUserCallback() {
                            @Override
                            public void onUserResult(boolean b, int i) {
                                d("更新访客结果：" + b + " —— " + i);
                            }
                        });
                    }
                } else {
                    FaceSDK.instance().addUser(faceId, headPath, new FaceUserManager.FaceUserCallback() {
                        @Override
                        public void onUserResult(boolean b, int i) {
                            d("添加访客结果：" + b + " —— " + i);
                        }
                    });
                }
            }
        }

        runnable.run();
    }

    private void downloadHead(final Queue<UserBean> queue, final Runnable runnable) {
        d("开始下载");
        if (queue == null || queue.size() <= 0) {
            runnable.run();
            return;
        }
        mCurrDownloadIndex++;
        setTextProgress(mCurrDownloadIndex + "/" + mUpdateTotal);

        final UserBean userBean = queue.poll();
        d("下载：" + userBean.getName() + " —— "+ userBean.getHead());
        if (!TextUtils.isEmpty(userBean.getHeadPath()) && new File(userBean.getHeadPath()).exists()) {
            d("本地头像地址为空");
            downloadHead(queue,runnable);
            return;
        }

        String headUrl = userBean.getHead();
        MyXutils.getInstance().downLoadFile(headUrl, userBean.getHeadPath(), false, new MyXutils.XDownLoadCallBack() {
            int downloadTag = 0;

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                Log.e(TAG, "下载进度... " + current + " —— " + total);
                setProgress((int) current, (int) total);
            }

            @Override
            public void onSuccess(File result) {
                Log.e(TAG, "下载成功... " + result.getPath());
                downloadTag = 0;
                userBean.setHeadPath(result.getPath());
            }

            @Override
            public void onError(Throwable ex) {
                d("下载失败... " + (ex != null ? ex.getMessage() : "NULL"));
                downloadTag = -1;
            }

            @Override
            public void onFinished() {
                userBean.setDownloadTag(downloadTag);
                long l = DaoManager.get().addOrUpdate(userBean);
                d("更新数据结果... " + l);
                downloadHead(queue,runnable);
            }
        });
    }

    public void destory() {
        OkHttpUtils.getInstance().cancelTag(this);
        if (floatSyncView != null) {
            floatSyncView.dismiss();
            floatSyncView = null;
        }
        try {
            mAct.unregisterReceiver(this);
        } catch (Exception e) {
            Log.d(TAG, TAG + "广播未注册");
        }
    }

    /***
     * 带UI更新的请求回调
     * @param <T>
     */
    abstract class MyStringCallback<T> extends StringCallback {
        private String title;
        private int step;
        public static final int STEP_COMPANY = 1;
        public static final int STEP_STAFF = 3;
        public static final String TITLE_COMPANY = "公司信息";
        public static final String TITLE_STAFF = "员工信息";
        private Handler handler = new Handler(Looper.getMainLooper());

        public MyStringCallback(int s) {
            step = s;
            switch (step) {
                case STEP_COMPANY:
                    title = TITLE_COMPANY;
                    break;
                case STEP_STAFF:
                    title = TITLE_STAFF;
                    break;
            }
        }

        public abstract void onRetryAfter5s();

        public abstract void onFailed();

        public abstract void onSucc(String response, T t);

        @Override
        public void onBefore(Request request, int id) {
            super.onBefore(request, id);
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            String err = "请求失败";
            if (e != null && (!TextUtils.isEmpty(e.getMessage()))) {
                if (e.getMessage().contains("404")) {
                    err = "服务器异常";
                } else if (e.getMessage().contains("500")) {
                    err = "服务器异常";
                }
            }
            if (isLocalServ || isNetworkConnected(APP.getContext())) {
                setErrInfo(err + "，5秒后重试...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRetryAfter5s();
                    }
                }, 5 * 1000);
            } else {
                setErrInfo("同步失败，请检查网络连接");
                onFailed();
            }
        }

        @Override
        public void onResponse(String response, int id) {
            d(TAG, response);
            Object o = null;
            if (step == STEP_COMPANY) {
                if (TextUtils.isEmpty(response)) {
                    setErrInfo("同步失败，Response is null");
                    onFailed();
                    return;
                }

                if (!isJSONValid(response)) {
                    setErrInfo("同步失败，Response is not JSON");
                    onFailed();
                    return;
                }

                CompanyBean bean = new Gson().fromJson(response, CompanyBean.class);

                if (bean.getStatus() != 1) {
                    String err = "同步失败，请检查网络或重启设备";
                    switch (bean.getStatus()) {
                        case 3://设备不存在（参数错误）
                            err = "设备不存在";
                            break;
                        case 4://设备未绑定
                            err = "请先绑定设备";
                            break;
                        case 5://未设置主题
                            err = "该设备未设置主题";
                            break;
                        default://获取失败
                            err = "同步失败，错误码：" + bean.getStatus();
                            break;
                    }
                    setErrInfo(err);
                    onFailed();
                    return;
                }
                o = bean;
            } else {//员工信息
                StaffBean staffInfo = new Gson().fromJson(response, StaffBean.class);
                if (staffInfo.getStatus() != 1) {
                    String err = "同步失败，请检查网络或重启设备";
                    switch (staffInfo.getStatus()) {
                        case 3://公司不存在
                            err = "公司不存在";
                            break;
                        case 4://公司未设置部门
                            err = "该公司未设置部门";
                            break;
                        default://参数错误
                            err = "数据异常，错误码：" + staffInfo.getStatus();
                            break;
                    }
                    setErrInfo(err);
                    onFailed();
                    return;
                }
                o = staffInfo;
            }
            onSucc(response, (T) o);
        }
    }

    /*======UI显示============================================================================================*/
    private Timer timer;

    private void startTimer(TimerTask timerTask, long delay) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(timerTask, delay);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /*===========判断方法=====================================================================================*/
    //判断网络连接
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private static final String TAG = "SyncManager";

    private void d(String log) {
        if (true) {
            Log.d(TAG, log);
        }
    }

    //日志打印不全
    public static void d(String tag, String msg) {  //信息太长,分段打印

        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，

        //  把4*1024的MAX字节打印长度改为2001字符数

        int max_str_length = 2001 - tag.length();

        //大于4000时

        while (msg.length() > max_str_length) {

            Log.i(tag, msg.substring(0, max_str_length));

            msg = msg.substring(max_str_length);

        }

        //剩余部分

        Log.d(tag, msg);

    }

    public final static boolean isJSONValid(String jsonInString) {
        try {
            new Gson().fromJson(jsonInString, Object.class);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }


    private void showUI() {
        if (mAct == null) {
            return;
        }
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                floatSyncView = new FloatSyncView(mAct);
                floatSyncView.show();
                floatSyncView.showProgress(false);
            }
        });
    }

    private void setErrInfo(final String info) {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setErrInfo(info);
                }
            });
        }
    }

    private void showProgress() {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.showProgress(true);
                }
            });
        }
    }

    private void setProgress(final int curr, final int total) {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setDownloadProgress(curr, total);
                }
            });
        }
    }

    private void setInfo(final String info) {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setNormalInfo(info);
                }
            });
        }
    }

    private void setTextProgress(final String progress) {
        if (floatSyncView != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    floatSyncView.setTvProgress(progress);
                }
            });
        }
    }

    private void closeNotSave() {
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (floatSyncView != null) {
                    setInfo("同步结束");
                    if (mListener != null) {
                        mListener.onFinish();
                    }

                    startTimer(new TimerTask() {
                        @Override
                        public void run() {
                            if (floatSyncView != null) {
                                floatSyncView.dismiss();
                            }
                        }
                    }, 3 * 1000);
                }
            }
        });
    }

    private void close() {
        SpUtils.saveLong(SpUtils.LAST_INIT_TIME, System.currentTimeMillis());
        EventBus.getDefault().postSticky(new EmployListActivity.EmployUpdate());
        closeNotSave();
    }

}
