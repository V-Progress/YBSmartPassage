package com.yunbiao.yb_smart_passage.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.Event.AdsAirInfoEvent;
import com.yunbiao.yb_smart_passage.activity.Event.IntroduceUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_passage.activity.fragment.ScreenSaveFragment;
import com.yunbiao.yb_smart_passage.business.AirQualityUtil;
import com.yunbiao.yb_smart_passage.business.LocateManager;
import com.yunbiao.yb_smart_passage.business.ResourceCleanManager;
import com.yunbiao.yb_smart_passage.business.PassageManager;
import com.yunbiao.yb_smart_passage.business.SyncManager;
import com.yunbiao.yb_smart_passage.db.PassageBean;
import com.yunbiao.yb_smart_passage.faceview.FaceResult;
import com.yunbiao.yb_smart_passage.faceview.FaceView;
import com.yunbiao.yb_smart_passage.utils.RestartAPPTool;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.yunbiao.yb_smart_passage.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

/**
 * Created by Administrator on 2018/11/26.
 */

public class WelComeActivity extends BaseGpioActivity {
    private static final String TAG = "WelComeActivity";
    private ImageView ivMainLogo;//公司logo
    private TextView tvMainAbbName;//公司名

    // xmpp推送服务
    private ServiceManager serviceManager;

    //摄像头分辨率
    private FaceView faceView;
    private TextSwitcher tsSignTips;
    private TextView tvTemp;
    private TextView tvWetness;
    private TextView tvPm25;
    private TextView tvCo2;
    private TextView tvVoc;
    private TextView tvHcho;
    private ScreenSaveFragment adsFragment;
    private View llTips;

    private final String DEFALUT_TIPS = "识别成功 na 欢迎光临";
    private final String DEFALUT_LEADER_TIPS = "欢迎 na po 第s次莅临指导";
    private ImageView ivLeft;
    private TextView tvQuality;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void initView() {
        APP.setActivity(this);
        faceView = findViewById(R.id.face_view);
        faceView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode ==  KeyEvent.KEYCODE_MENU){
                    goSetting();
                }
                return true;
            }
        });
        if(faceView != null){
            faceView.setCallback(faceCallback);
        }
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);
        tsSignTips = findViewById(R.id.ts_sign_tips);
        llTips = findViewById(R.id.ll_tips);
        tsSignTips.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(WelComeActivity.this);
                textView.setTextColor(Color.parseColor("#ffffff"));
                textView.setTextSize(46);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                return textView;
            }
        });

        tvTemp = findViewById(R.id.tv_main_temperature);
        tvWetness = findViewById(R.id.tv_main_wetness);
        tvPm25 = findViewById(R.id.tv_main_pm2_5);
        tvCo2 = findViewById(R.id.tv_main_co2);
        tvVoc = findViewById(R.id.tv_main_voc);
        tvHcho = findViewById(R.id.tv_main_hcho);
        ivLeft = findViewById(R.id.iv_main_left);
        tvQuality = findViewById(R.id.tv_main_quality);
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelComeActivity.this,IntroduceActivity.class));
            }
        });

        //加载广告Fragment
        adsFragment = new ScreenSaveFragment();
        addFragment(R.id.ll_face_main, adsFragment);
    }

    @Override
    protected void initData() {
        //开启Xmpp
        startXmpp();

        //初始化定位工具
        LocateManager.instance().init(this);

        setCompInfo();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: ------- ");
//                UpdateVersionControl.getInstance().checkUpdate(WelComeActivity.this);
            }
        },5 * 1000);
    }

    private long mCurrFaceId = 0;

    /*人脸识别回调，由上到下执行*/
    private FaceView.FaceCallback faceCallback = new FaceView.FaceCallback() {
        @Override
        public void onReady() {
            SyncManager.instance().init(WelComeActivity.this, loadListener);

            ResourceCleanManager.instance().startAutoCleanService();
        }

        @Override
        public void onFaceDetection(FaceResult result) {
            if (isAlwayOpen()) {
                return;
            }
            if (adsFragment != null) {
                adsFragment.detectFace();
            }

            long faceId = result.getBaseProperty().getFaceId();
            if(mCurrFaceId != faceId){
                mCurrFaceId = faceId;
                Log.e(TAG, "onFaceDetection: ----- 换人了");
                showToast("正在验证");
            }
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            if(isAlwayOpen()){
                return;
            }
            PassageManager.instance().checkSign(verifyResult);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(IntroduceUpdateEvent updateEvent) {
        d("update: ----- 收到气候信息更新事件");
        boolean hasData = updateEvent.isHasData();
        ivLeft.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private SyncManager.LoadListener loadListener = new SyncManager.LoadListener() {
        @Override
        public void onLoaded() {
            EventBus.getDefault().postSticky(new SysInfoUpdateEvent());

            PassageManager.instance().init(WelComeActivity.this, signEventListener);

            setCompInfo();
        }

        @Override
        public void onLogoLoded() {
            setCompInfo();
        }

        @Override
        public void onFinish() {
        }
    };

    private PassageManager.SignEventListener signEventListener = new PassageManager.SignEventListener() {
        @Override
        public void onSigned(final PassageBean passageBean) {
            Log.e(TAG, "onSigned: " + passageBean.toString());
            showToast("签到成功："  + passageBean.getName());

            openDoor();
        }

        @Override
        public void onVerifyFailed() {
            showToast("验证失败");
        }
    };

    private void showToast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UIUtils.showShort(WelComeActivity.this,msg);
            }
        });
    }

    private void startXmpp() {//开启xmpp
        serviceManager = new ServiceManager(this);
        serviceManager.startService();
    }

    private void destoryXmpp() {
        if (serviceManager != null) {
            serviceManager.stopService();
            serviceManager = null;
        }
    }

    private void setCompInfo(){
        String compName = SpUtils.getStr(SpUtils.COMPANY_NAME);
        String logoPath = SpUtils.getStr(SpUtils.COMPANY_LOGO);

        if (!TextUtils.isEmpty(compName)) tvMainAbbName.setText(compName);

        if (!TextUtils.isEmpty(logoPath) && new File(logoPath).exists()) bindImageView(logoPath, ivMainLogo);
    }

    //开始隐藏计时
    private void startGoneDelay() {
        llTips.removeCallbacks(runnable);
        llTips.postDelayed(runnable, 2000);
    }

    //隐藏提示
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            llTips.setVisibility(View.GONE);
        }
    };

    //密码弹窗
    private void inputPwd(final Runnable runnable) {
        String pwd = SpUtils.getStr(SpUtils.MENU_PWD);
        if (TextUtils.isEmpty(pwd)) {
            if (runnable != null) {
                runnable.run();
            }
            return;
        }
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_input_pwd);

        final Animation animation = AnimationUtils.loadAnimation(WelComeActivity.this, R.anim.anim_edt_shake);
        final View rootView = dialog.findViewById(R.id.ll_input_pwd);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_input_confirm);
        final EditText edtPwd = (EditText) dialog.findViewById(R.id.edt_input_pwd);
        Button btnBack = dialog.findViewById(R.id.btn_input_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = edtPwd.getText().toString();
                if (TextUtils.isEmpty(pwd)) {
                    edtPwd.setError("不要忘记输入密码哦");
                    rootView.startAnimation(animation);
                    return;
                }
                String spPwd = SpUtils.getStr(SpUtils.MENU_PWD);
                if (!TextUtils.equals(pwd, spPwd)) {
                    edtPwd.setError("密码错了，重新输入吧");
                    rootView.startAnimation(animation);
                    return;
                }
                if (runnable != null) {
                    runnable.run();
                }
                dialog.dismiss();
            }
        });

        dialog.show();

        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.mystyle);  //添加动画
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void goSetting() {
        inputPwd(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelComeActivity.this, SystemActivity.class));
            }
        });
    }

    //跳转设置界面
    public void goSetting(View view) {
        goSetting();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: ------ " + event.getAction());
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.e(TAG, "onKeyDown: ------ 111111111 " + event.getAction());
            goSetting();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        RestartAPPTool.showExitDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputPwd(new Runnable() {
                    @Override
                    public void run() {
                        moveTaskToBack(true);
                    }
                });
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputPwd(new Runnable() {
                    @Override
                    public void run() {
                        APP.exit();
                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(faceView != null){
            faceView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(faceView != null){
            faceView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(faceView != null){
            faceView.destory();
        }
        destoryXmpp();

        SyncManager.instance().destory();
        LocateManager.instance().destory();
    }
}