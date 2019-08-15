package com.yunbiao.yb_smart_passage.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.activity.WelComeActivity;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.yunbiao.yb_smart_passage.utils.xutil.MyXutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private Activity mContext;

    //返回的安装包url
    private static String apkUrl = "http://211.157.160.102/imgserver/hsd.apk";

    /* 下载包安装路径 */
    private static final String savePath = "/sdcard/mnt/sdcard/hsd/apk/";

    private static final String saveFileName = savePath + "hsd.apk";

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private int progress;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    if (onReceivedProgressRun != null) {
                        onReceivedProgressRun.OnProgressRunReceived(progress);
                    }
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public interface OnReceivedProgressRun {
        void OnProgressRunReceived(int progress);
    }
    public static OnReceivedProgressRun onReceivedProgressRun;

    public static void setOnReceivedProgressRun(OnReceivedProgressRun progressRun) {
        onReceivedProgressRun = progressRun;
    }

    public UpdateManager(Activity context) {
        this.mContext = context;
    }

    //	//外部接口让主Activity调用
    public void checkUpdateInfo(String version) {
        Log.e(TAG, "检测更新... ");
        Log.e(TAG, "当前版本号... " + version);
        Log.e(TAG, "检测更新... " + ResourceUpdate.VERSION_URL);
        //判断是否需要更新
        final HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("clientVersion", version);
        paramMap.put("type", 1+ "");
        MyXutils.getInstance().post(ResourceUpdate.VERSION_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.e("UpdateManager", "checkUpdateInfo------------->"+result );
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                judgeIsUpdate(result);
            }

            @Override
            public void onError(Throwable ex) {
                MyXutils.getInstance().post("http://10.128.237.164/device/service/getversion.html", paramMap, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e("UpdateManager", "checkUpdateInfo------------->"+result );
                        if (result.startsWith("\"")) {
                            result = result.substring(1, result.length() - 1);
                        }
                        judgeIsUpdate(result);
                    }

                    @Override
                    public void onError(Throwable ex) {
                    }

                    @Override
                    public void onFinish() {
                    }
                });
            }

            @Override
            public void onFinish() {

            }
        });
    }

    private void judgeIsUpdate(String isUpdate) {
        //返回
        switch (isUpdate) {
            case "1": //不需要更新
                if(mContext != null){
                    UIUtils.showShort(mContext,"当前版本为最新版本");
                }
                if (UIUtils.pd != null && UIUtils.pd.isShowing()) {
                    UIUtils.pd.dismiss();
                }
                break;
            case "faile":  //网络不通，或者解析出错
                if(mContext != null){
                    UIUtils.showShort(mContext,"网络连接失败，请检查网络");
                }
                if (UIUtils.pd != null && UIUtils.pd.isShowing()) {
                    UIUtils.pd.dismiss();
                }
                break;
            default:
                if(mContext != null){
                    UIUtils.updatePd(mContext);
                }
                setOnReceivedProgressRun(new OnReceivedProgressRun() {
                    @Override
                    public void OnProgressRunReceived(int progress) {
                        UIUtils.pd.setProgress(progress);//给进度条设置数值
                        if (progress == 100) {
                            UIUtils.pd.dismiss();
                        }
                    }
                });

                apkUrl = isUpdate;
                //下载apk
                downloadApk();
                break;
        }
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(30000);
                conn.setReadTimeout(360000);
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdirs();
                }

                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];
                boolean interceptFlag = false;
                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                WelComeActivity activity = APP.getActivity();
                if(activity != null){
                    UIUtils.showShort(activity,"下载失败");
                }
                if (UIUtils.pd != null && UIUtils.pd.isShowing()) {
                    UIUtils.pd.dismiss();
                }
            }
        }
    };

    /**
     * 下载apk
     *
     * @param
     */
    private void downloadApk() {
        Thread downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk() {
        if (UIUtils.pd != null && UIUtils.pd.isShowing()) {
            UIUtils.pd.dismiss();
        }

        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}
