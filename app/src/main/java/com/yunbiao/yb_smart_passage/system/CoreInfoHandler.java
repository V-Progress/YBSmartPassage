package com.yunbiao.yb_smart_passage.system;

import android.app.ProgressDialog;
import android.util.Log;


import com.yunbiao.yb_smart_passage.activity.Event.AdsUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.Event.GpioEvent;
import com.yunbiao.yb_smart_passage.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.WelComeActivity;
import com.yunbiao.yb_smart_passage.activity.fragment.ResourceResolver;
import com.yunbiao.yb_smart_passage.business.SyncManager;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.common.MachineDetial;
import com.yunbiao.yb_smart_passage.common.SoundControl;
import com.yunbiao.yb_smart_passage.common.UpdateVersionControl;
import com.yunbiao.yb_smart_passage.common.power.PowerOffTool;
import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.utils.*;
import com.yunbiao.yb_smart_passage.utils.CommonUtils;
import com.yunbiao.yb_smart_passage.utils.logutils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * xmpp消息处理
 *
 * @author Administrator
 */
public class CoreInfoHandler {
    private static final String TAG = "CoreInfoHandler";

    private static final int ONLINE_TYPE = 1;// 上线
    private static final int VOICE_TYPE = 3;// 声音
    private static final int CUTSCREN_TYPE = 4;// 截屏
    private static final int RUNSET_TYPE = 5;// 设备开关机设置
    private static final int SHOW_SERNUM = 6;// 显示设备编号
    private static final int SHOW_VERSION = 7;// 显示版本号
    private static final int SHOW_DISK_IFNO = 8;// 获取磁盘容量
    private static final int POWER_RELOAD = 9;// 设备 开机 重启
    private static final int PUSH_TO_UPDATE = 10;//软件升级
    private final static int ADS_PUSH = 23;//广告更新推送
    private final static int UPDATE_STAFF = 26;//员工信息更新
    private final static int OPEN_DOOR = 99; //开门
    private final static int ALWAYS_OPEN = 100;//门禁常开
    private final static int UPDATE_COMPANY = 101;//更新公司信息
    private final static int UPDATE_INTRODUCE = 33;

    public static boolean isOnline = false;

    public static void messageReceived(String message) {
        LogUtils.e(TAG, "接收消息：" + message);
        try {
            JSONObject mesJson = new JSONObject(message);
            Integer type = mesJson.getInt("type");
            JSONObject contentJson;
            switch (type.intValue()) {
                case ONLINE_TYPE:
                    int comId = SpUtils.getInt(SpUtils.COMPANY_ID);
                    if(comId == 0){
                        SyncManager.instance().initInfo();
                    }
                    // 系统登录
                    contentJson = mesJson.getJSONObject("content");
                    if (!contentJson.isNull("serNum")) {
                        MachineDetial.getInstance().upLoadHardWareMessage();

                        String bindCode = CommonUtils.getJsonObj(contentJson, "pwd", "").toString();
                        SpUtils.saveStr(SpUtils.BINDCODE,bindCode);
//                     //第一次系统启动的时候服务器没有设备详细信息，需要向设备传消息
                        String serNum = (String) CommonUtils.getJsonObj(contentJson, "serNum", "");
                        SpUtils.saveStr(SpUtils.DEVICE_NUMBER, serNum);
                        //设备过期时间
                        String expireDate = CommonUtils.getJsonObj(contentJson, "expireDate", "").toString();
                        SpUtils.saveStr(SpUtils.EXP_DATE,expireDate);
                        isOnline = true;
                    }

                    String runKey = (String) CommonUtils.getJsonObj(contentJson, "runKey", "");
                    SpUtils.saveStr(SpUtils.RUN_KEY,runKey);

                    Integer dtype = (Integer) CommonUtils.getJsonObj(contentJson, "dtype", -1);
                    SpUtils.saveInt(SpUtils.DEVICE_TYPE,dtype);

                    Log.e(TAG, "messageReceived: ---- 发送系统信息更新事件");
                    EventBus.getDefault().postSticky(new SysInfoUpdateEvent());
                    break;
                case VOICE_TYPE:// 声音控制
                    JSONObject jsonObject = mesJson.getJSONObject("content");
                    if (jsonObject != null) {
                        SoundControl.setMusicSound(jsonObject.getDouble("voice"));
                    }
                    break;
                case CUTSCREN_TYPE:
                    final ScreenShotUtil instance = ScreenShotUtil.getInstance();
                    instance.takeScreenshot(APP.getContext(), new ScreenShotUtil.ScreenShotCallback() {
                        @Override
                        public void onShotted(boolean isSucc, String filePath) {
                            String sid = HeartBeatClient.getDeviceNo();
                            instance.sendCutFinish(sid,filePath);
                        }
                    });
                    break;
                case RUNSET_TYPE:
                    ThreadUitls.runInThread(new Runnable() {
                        @Override
                        public void run() {// 开关机时间设置
                            PowerOffTool.getPowerOffTool().getPowerOffTime(HeartBeatClient.getDeviceNo());
                        }
                    });
                    break;
                case SHOW_SERNUM:
                    contentJson = (JSONObject) CommonUtils.getJsonObj(mesJson, "content", null);
                    if (contentJson != null) {
                        Integer showType = (Integer) CommonUtils.getJsonObj(contentJson, "showType", null);
                        if (showType != null && showType == 0) {//状态栏  视美泰主板
                            Integer showValue = (Integer) CommonUtils.getJsonObj(contentJson, "showValue", null);
                            if (showValue == 0) {//显示
                                APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
                            } else if (showValue == 1) {//隐藏
                                APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), false);
                            }
                        } else { // 显示设备编号
                            UIUtils.showTitleTip(APP.getContext(),SpUtils.getStr(SpUtils.DEVICE_NUMBER));
                        }
                    }
                    break;
                case SHOW_VERSION:// 版本信息
                    ResourceUpdate.uploadAppVersion();
                    break;
                case SHOW_DISK_IFNO:
                    contentJson = mesJson.getJSONObject("content");
                    Integer flag = (Integer) CommonUtils.getJsonObj(contentJson, "flag", null);
                    if (flag != null) {
                        if (flag == 0) { //显示
                            ResourceUpdate.uploadDiskInfo();
                        } else if (flag == 1) {// 清理磁盘
                            ResourceUpdate.uploadDiskInfo();
                        }
                    }
                    break;
                case POWER_RELOAD:// 机器重启
                    contentJson = mesJson.getJSONObject("content");
                    Integer restart = (Integer) CommonUtils.getJsonObj(contentJson, "restart", null);
                    if (restart != null) {
                        if (restart == 0) {
                            ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(APP.getContext());
                            progressDialog.setTitle("关机");
                            progressDialog.setMessage("3秒后将关闭设备");
                            progressDialog.show();
                            UIUtils.powerShutDown.start();
                        } else if (restart == 1) {
                            ProgressDialog progressDialog = UIUtils.coreInfoShow3sDialog(APP.getContext());
                            progressDialog.setTitle("重启");
                            progressDialog.setMessage("3秒后将重启设备");
                            progressDialog.show();
                            UIUtils.restart.start();
                        }
                    }
                    break;
                case PUSH_TO_UPDATE:
                    WelComeActivity activity = APP.getActivity();
                    UpdateVersionControl.getInstance().checkUpdate(activity);
                    break;
                case ADS_PUSH:
                    EventBus.getDefault().postSticky(new AdsUpdateEvent());
                    break;
                case UPDATE_STAFF:
                    SyncManager.instance().initInfo();
                    break;
                case OPEN_DOOR:
                    EventBus.getDefault().postSticky(new GpioEvent(GpioEvent.OPEN));
                    break;
                case ALWAYS_OPEN:
                    boolean doorState = SpUtils.getBoolean(SpUtils.DOOR_STATE, false);
                    boolean newState = !doorState;
                    EventBus.getDefault().postSticky(new GpioEvent(newState));
                    SpUtils.saveBoolean(SpUtils.DOOR_STATE,newState);
                    break;
                case UPDATE_COMPANY:
                    SyncManager.instance().loadCompany(false);
                    break;
                case UPDATE_INTRODUCE:
                    ResourceResolver.instance().init();
                    break;
                default:
                    break;
            }
        } catch (JSONException /*| UnsupportedEncodingException*/ e) {
            e.printStackTrace();
        }
    }

}