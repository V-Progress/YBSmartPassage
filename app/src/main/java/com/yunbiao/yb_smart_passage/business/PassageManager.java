package com.yunbiao.yb_smart_passage.business;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jdjr.risk.face.local.user.FaceUser;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.Config;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.bean.BaseResponse;
import com.yunbiao.yb_smart_passage.db2.PassageBean;
import com.yunbiao.yb_smart_passage.db2.DaoManager;
import com.yunbiao.yb_smart_passage.db2.UserBean;
import com.yunbiao.yb_smart_passage.db2.VisitorBean;
import com.yunbiao.yb_smart_passage.system.HeartBeatClient;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;

/**
 * Created by Administrator on 2019/3/18.
 */

public class PassageManager {
    private final String TAG = getClass().getSimpleName();
    private static PassageManager instance;
    private final int UPDATE_TIME = 20;
    private SignEventListener listener;
    private String today;
    private Activity mAct;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private DateFormat passageDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final ExecutorService threadPool;
    private final ScheduledExecutorService autoUploadThread;
    private long verifyOffsetTime = 2000;//验证间隔时间
    private boolean isDebug = true;

    private final int TAG_MAX_TIME = 5;
    private int verifyTag = 0;
    private int queryTag = 0;

    private Map<String, Long> passageMap = new HashMap<>();

    public static PassageManager instance() {
        if (instance == null) {
            synchronized (PassageManager.class) {
                if (instance == null) {
                    instance = new PassageManager();
                }
            }
        }
        return instance;
    }

    private PassageManager() {
        //初始化当前时间
        today = dateFormat.format(new Date());
        threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(initRunnable);

        autoUploadThread = Executors.newSingleThreadScheduledExecutor();
        autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 5, 600, TimeUnit.SECONDS);
    }

    //初始化线程
    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            final List<PassageBean> passageBeans = DaoManager.get().queryByPassDate(today);
            if(passageBeans != null){
                for (PassageBean passageBean : passageBeans) {
                    long passTime = passageBean.getPassTime();
                    String faceId = passageBean.getFaceId();
                    if(passageMap.containsKey(faceId)){
                        Long time = passageMap.get(faceId);
                        if(passTime > time){
                            passageMap.put(faceId,passTime);
                        } else {
                            continue;
                        }
                    }
                }
            }
            clearJDVerifyRecord();
        }
    };

    //定时发送签到数据
    private Runnable autoUploadRunnable = new Runnable() {
        @Override
        public void run() {
            String currDate = dateFormat.format(new Date());
            if (!TextUtils.equals(currDate, today)) {
                today = currDate;
                passageMap.clear();
                initRunnable.run();
            }

            final List<PassageBean> passList = DaoManager.get().queryByPassUpload(false);
            if (passList == null) {
                Log.e(TAG, "run: ------1 未上传：" + passList.size());
                return;
            }
            Log.e(TAG, "run: ------2 未上传：" + passList.size());

            if (passList.size() <= 0) {
                return;
            }

            d("上传签到记录... " + ResourceUpdate.SIGNARRAY);
            Map<String, String> params = new HashMap<>();
            params.put("deviceNo", HeartBeatClient.getDeviceNo());
            params.put("comId", SpUtils.getInt(SpUtils.COMPANY_ID) + "");

            Map<String, File> fileMap = new HashMap<>();
            List<SignDataBean> signDataBeans = new ArrayList<>();
            for (PassageBean passageBean : passList) {
                SignDataBean signDataBean = new SignDataBean();
                signDataBean.entryId = passageBean.getEntryId();
                signDataBean.createTime = passageBean.getPassTime();
                signDataBean.similar = passageBean.getSimilar();
                signDataBean.card = TextUtils.isEmpty(passageBean.getCard()) ? "" : passageBean.getCard() ;
                signDataBean.isPass = passageBean.getIsPass();
                signDataBeans.add(signDataBean);

                String headPath = passageBean.getHeadPath();
                File imgFile = null;
                if (!TextUtils.isEmpty(headPath)) {
                    imgFile = new File(headPath);
                }

                if (imgFile == null || !imgFile.exists()) {
                    imgFile = new File(Environment.getExternalStorageDirectory() + "/1.txt");
                    if (!imgFile.exists()) {
                        try {
                            imgFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                fileMap.put(imgFile.getName(), imgFile);
            }

            String jsonStr = new Gson().toJson(signDataBeans);
            params.put("witJson",jsonStr);

            d("记录条数：" + signDataBeans.size());
            d("文件数量：" + fileMap.size());

            d("参数... " + params.toString());
            d("参数... " + fileMap.toString());

            OkHttpUtils.post()
                    .url(ResourceUpdate.SIGNARRAY)
                    .params(params)
                    .files("heads", fileMap)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            d("上送失败... " + (e != null ? e.getMessage() : "NULL"));
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            d("上送成功... " + response);
                            JSONObject jsonObject = JSONObject.parseObject(response);
                            String status = jsonObject.getString("status");
                            if (!TextUtils.equals("1", status)) {
                                return;
                            }

                            threadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    for (PassageBean passageBean : passList) {

                                        passageBean.setUpload(true);
                                        long update = DaoManager.get().update(passageBean);
                                        d("更新结果... " + update);
                                    }
                                }
                            });
                        }
                    });

            clearJDVerifyRecord();
        }
    };

    public PassageManager init(@NonNull Activity mAct, @NonNull SignEventListener signEventListener) {
        this.mAct = mAct;
        listener = signEventListener;

        return instance();
    }

    public void checkSign(VerifyResult verifyResult) {
        int resultCode = verifyResult.getResult();
        if (resultCode != VerifyResult.UNKNOWN_FACE) {
            d("识别失败 " + resultCode);
            verifyTag ++;
            if(verifyTag >= TAG_MAX_TIME){
                verifyTag = 0;
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVerifyFailed();
                    }
                });
            }
            return;
        }
        verifyTag = 0;

        byte[] faceImageBytes = verifyResult.getFaceImageBytes();
        FaceUser user = verifyResult.getUser();
        if (user == null || TextUtils.isEmpty(user.getUserId())) {
            return;
        }

        //生成签到时间
        final long currTime = System.currentTimeMillis();
        final PassageBean passageBean = new PassageBean();
        passageBean.setCreateDate(dateFormat.format(currTime));
        passageBean.setFaceId(user.getUserId());
        passageBean.setPassTime(currTime);
        passageBean.setSimilar(verifyResult.getCheckScore()+"");

        d("开始签到... ");
        int faceId = Integer.parseInt(user.getUserId());
        if(faceId < 0){//如果小于0 走访客流程
            checkVisitor(passageBean,faceImageBytes);
        } else {//如果大于0 走员工流程
            checkUser(passageBean,faceImageBytes);
        }
    }

    private void checkUser(final PassageBean passageBean, byte[] imgBytes){
        d("是员工");

        String faceId = passageBean.getFaceId();
        List<UserBean> UserBeans = DaoManager.get().queryUserByFaceId(faceId);
        if(UserBeans == null || UserBeans.size() <= 0){
            d("库中无此人... ");
            queryTag++;
            if(queryTag >= TAG_MAX_TIME){
                queryTag = 0;
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVerifyFailed();
                    }
                });
            }
            return;
        }
        queryTag = 0;

        if(!canPass(passageBean)){
            d("时间未到... ");
            return;
        }

        UserBean userBean = UserBeans.get(0);
        Log.e(TAG, "checkUser: ---- " + userBean.toString());
        passageBean.setEntryId(userBean.getId());
        passageBean.setSex(userBean.getSex());
        passageBean.setName(userBean.getName());
        passageBean.setUserType(0);

        File imgFile = saveBitmap(passageBean.getPassTime(), imgBytes);
        passageBean.setHeadPath(imgFile.getPath());

        d("员工通行... " + passageBean.getName() + " --- " + imgFile.getPath());
        onPass(passageBean);
    }

    private void checkVisitor(final PassageBean passageBean, byte[] imgBytes){
        d("是访客");

        String faceId = passageBean.getFaceId();
        VisitorBean visitorBean = DaoManager.get().queryByVisitorFaceId(faceId);
        if(visitorBean == null){
            d("库中无此人... ");
            queryTag++;
            if(queryTag >= TAG_MAX_TIME){
                queryTag = 0;
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVerifyFailed();
                    }
                });
            }
            return;
        }
        queryTag = 0;

        if(!canPass(passageBean)){
            d("时间未到... ");
            return;
        }

        String currStartStr = visitorBean.getCurrStart();
        String currEndStr = visitorBean.getCurrEnd();

        String currDateStr = passageDateFormat.format(new Date(passageBean.getPassTime()));

        try {
            Date currDate = passageDateFormat.parse(currDateStr);
            Date startDate = passageDateFormat.parse(currStartStr);
            Date endDate = passageDateFormat.parse(currEndStr);
            //如果当前时间早于开始时间 或者 晚于结束时间则不允许通过
            if(currDate.before(startDate)){
                onNotTime(-1,passageBean);
                return;
            } else if(currDate.after(endDate)){
                onNotTime(-9,passageBean);
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        passageBean.setEntryId(visitorBean.getId());
        passageBean.setSex(visitorBean.getSex());
        passageBean.setName(visitorBean.getName());
        passageBean.setUserType(-1);
        passageBean.setVisiId(visitorBean.getId());

        File imgFile = saveBitmap(passageBean.getPassTime(), imgBytes);
        passageBean.setHeadPath(imgFile.getPath());

        d("访客通行... " + passageBean.getName() + " --- " + imgFile.getPath());
        onPass(passageBean);
    }

    private void onNotTime(final int tag, final PassageBean passageBean){
        if(listener != null){
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onNotTime(tag,passageBean);
                }
            });
        }
    }

    private void onPass(final PassageBean passageBean){
        if (listener != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onPass(passageBean);
                }
            });
        }

        sendPassageRecord(passageBean);
    }

    private void sendPassageRecord(final PassageBean passageBean){
        d("准备上传记录... ");

        Map<String,String> params = new HashMap<>();
        params.put("deviceNo",HeartBeatClient.getDeviceNo());
        params.put("comId",SpUtils.getInt(SpUtils.COMPANY_ID) + "");

        String url;
        if(passageBean.getUserType() != -1){
            params.put("entryId", passageBean.getEntryId()+"");
            params.put("similar", passageBean.getSimilar()+"");
            params.put("card", passageBean.getCard()+"");
            params.put("isPass", passageBean.getIsPass()+"");
            url = ResourceUpdate.SIGNLOG;
        } else {
            params.put("visitorId",passageBean.getVisiId()+"");
            url = ResourceUpdate.VISITOR_RECORD;
        }
        d("接口地址：" + url);
        d("参数：" + params.toString());
        PostFormBuilder builder = OkHttpUtils.post().url(url).params(params);

        String headPath = passageBean.getHeadPath();
        File imgFile = new File(headPath);
        if (imgFile != null && imgFile.exists()) {
            builder.addFile("heads", imgFile.getName(), imgFile);
        }
        d("文件参数：heades = " + imgFile.getName() + "," + imgFile.getPath());

        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                d("上传记录失败---- " +(e == null?"null":e.getMessage()));
                passageBean.setUpload(false);
                DaoManager.get().addOrUpdate(passageBean);
            }

            @Override
            public void onResponse(String response, int id) {
                d("上传记录响应---- "+response);
                if(TextUtils.isEmpty(response)){
                    return;
                }
                BaseResponse baseResponse = new Gson().fromJson(response, BaseResponse.class);
                if(baseResponse == null){
                    return;
                }
                if(baseResponse.getStatus() != 1){
                    return;
                }
                passageBean.setUpload(true);
                DaoManager.get().addOrUpdate(passageBean);
            }
        });
    }

    private boolean canPass(PassageBean passageBean) {
        String faceId = passageBean.getFaceId();
        if (!passageMap.containsKey(faceId)) {
            passageMap.put(faceId, passageBean.getPassTime());
            return true;
        }

        long lastTime = passageMap.get(faceId);
        long currTime = passageBean.getPassTime();
        boolean isCanPass = (currTime - lastTime) > verifyOffsetTime;
        if (isCanPass) {
            passageMap.put(faceId, currTime);
        }
        return isCanPass;
    }

    class SignDataBean {
        long entryId;
        public int isPass;
        String card;
        String similar;
        long createTime;
    }

    private void d(@NonNull String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public File saveBitmap(long time, byte[] mBitmapByteArry) {
        long start = System.currentTimeMillis();
        File filePic;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap image = BitmapFactory.decodeByteArray(mBitmapByteArry, 0, mBitmapByteArry.length, options);

            //格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(time);
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String sdfTime = sdf.format(time);
            filePic = new File(Constants.RECORD_PATH + "/" + today + "/" + sdfTime + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            image.compress(Bitmap.CompressFormat.JPEG, Config.getCompressRatio(), fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        long end = System.currentTimeMillis();
        Log.e("Compress", "saveBitmap: 压缩耗时----- " + (end - start));
        return filePic;
    }

    /*定时清除京东SDK验证记录*/
    private void clearJDVerifyRecord() {
        int count = 0;
        int failed = 0;
        File dirFile = new File(APP.getContext().getDir("VerifyRecord", Context.MODE_PRIVATE).getAbsolutePath());
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file != null) {
                if (file.delete()) {
                    count++;
                } else {
                    failed++;
                }
            } else {
                failed++;
            }
        }
        Log.e(TAG, "总共清除记录：" + count + "条" + "，失败：" + failed + "条");
    }

    public interface SignEventListener {
        void onPass(PassageBean passageBean);
        void onNotTime(int timeTag,PassageBean passageBean);
        void onVerifyFailed();
    }

}