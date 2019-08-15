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
import com.yunbiao.yb_smart_passage.db.PassageBean;
import com.yunbiao.yb_smart_passage.db.SignDao;
import com.yunbiao.yb_smart_passage.db.UserBean;
import com.yunbiao.yb_smart_passage.db.UserDao;
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
    private SignDao signDao;
    private final int UPDATE_TIME = 20;
    private SignEventListener listener;
    private String today;
    private Activity mAct;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private final ExecutorService threadPool;
    private final ScheduledExecutorService autoUploadThread;
    private long verifyOffsetTime = 8000;//验证间隔时间
    private final UserDao userDao;
    private boolean isDebug = true;

    private final int TAG_MAX_TIME = 6;
    private int verifyTag = 0;

    private Map<Integer, Long> passageMap = new HashMap<>();

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
        signDao = APP.getSignDao();
        userDao = APP.getUserDao();

        threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(initRunnable);

        autoUploadThread = Executors.newSingleThreadScheduledExecutor();
        autoUploadThread.scheduleAtFixedRate(autoUploadRunnable, 5, UPDATE_TIME, TimeUnit.MINUTES);
    }

    //初始化线程
    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            final List<PassageBean> passageBeans = signDao.queryByDate(today);
            if(passageBeans != null){
                for (PassageBean passageBean : passageBeans) {
                    long passTime = passageBean.getPassTime();
                    int id = passageBean.getId();
                    if(passageMap.containsKey(id)){
                        Long time = passageMap.get(id);
                        if(passTime > time){
                            passageMap.put(id,passTime);
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

            final List<PassageBean> passList = signDao.queryByIsUpload(false);
            if (passList == null) {
                return;
            }
            Log.e(TAG, "run: ------ 未上传：" + passList.size());

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
                signDataBean.entryId = passageBean.getId();
                signDataBean.createTime = passageBean.getPassTime();
                signDataBean.similar = passageBean.getSimilar();
                signDataBean.card = passageBean.getCard();
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

            d("参数... " + params.toString());
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
                                        int update = signDao.update(passageBean);
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
        if (user == null) {
            return;
        }
        String userId = user.getUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        d("开始签到... ");
        final long currTime = System.currentTimeMillis();
        final PassageBean passageBean = new PassageBean();
        passageBean.setDate(dateFormat.format(currTime));
        passageBean.setFaceId(Integer.valueOf(userId));
        passageBean.setPassTime(currTime);
        if(!canPass(passageBean)){
            d("时间未到... ");
            return;
        }

        List<UserBean> userBeans = userDao.queryByFaceId(Integer.valueOf(userId));
        if (userBeans == null || userBeans.size() <= 0) {
            d("库中无此人... ");
            return;
        }
        UserBean userBean = userBeans.get(0);
        //如果需要统计次数则递增并更新数据库
        passageBean.setEntryId(userBean.getId());
        passageBean.setSex(userBean.getSex());
        passageBean.setName(userBean.getName());
        passageBean.setSimilar(verifyResult.getCheckScore()+"");
        passageBean.setDepartName(userBean.getDepartName());

        d("签到成功... " + passageBean.getName());
        if (listener != null) {
            mAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSigned(passageBean);
                }
            });
        }

        File imgFile = saveBitmap(currTime, faceImageBytes);
        d("签到成功... " + imgFile.getPath());
        passageBean.setHeadPath(imgFile.getPath());
        sendSignRecord(passageBean);
    }

    private void sendSignRecord(final PassageBean passageBean) {
        String deviceNo = HeartBeatClient.getDeviceNo();
        int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
        final Map<String, String> params = new HashMap<>();
        params.put("entryId", passageBean.getEntryId()+"");
        params.put("similar", passageBean.getSimilar()+"");
        params.put("card", passageBean.getCard()+"");
        params.put("isPass", passageBean.getIsPass()+"");
        params.put("deviceNo", deviceNo+"");
        params.put("comId", comId+"");
        PostFormBuilder builder = OkHttpUtils.post().url(ResourceUpdate.SIGNLOG).params(params);

        String headPath = passageBean.getHeadPath();
        File imgFile = new File(headPath);
        if (imgFile != null && imgFile.exists()) {
            builder.addFile("heads", imgFile.getName(), imgFile);
        }
        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG, "onError: " + e != null ? e.getMessage() : "NULL");
                passageBean.setUpload(false);
                signDao.insert(passageBean);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + response);
                JSONObject jsonObject = JSONObject.parseObject(response);
                String status = jsonObject.getString("status");
                passageBean.setUpload(TextUtils.equals("1", status));
                signDao.insert(passageBean);
            }

            @Override
            public void onAfter(int id) {

            }
        });
    }

    private boolean canPass(PassageBean passageBean) {
        int faceId = passageBean.getFaceId();
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
        public int isPass;
        int entryId;
        long createTime;
        int stat;
        String similar;
        String card;
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
        void onSigned(PassageBean passageBean);
        void onVerifyFailed();
    }

}