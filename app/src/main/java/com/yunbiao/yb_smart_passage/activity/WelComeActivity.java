package com.yunbiao.yb_smart_passage.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jdjr.risk.face.local.verify.VerifyResult;
import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.Event.IntroduceUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.base.BaseGpioActivity;
import com.yunbiao.yb_smart_passage.activity.fragment.ScreenSaveFragment;
import com.yunbiao.yb_smart_passage.business.LocateManager;
import com.yunbiao.yb_smart_passage.business.ResourceCleanManager;
import com.yunbiao.yb_smart_passage.business.PassageManager;
import com.yunbiao.yb_smart_passage.business.SyncManager;
import com.yunbiao.yb_smart_passage.db.PassageBean;
import com.yunbiao.yb_smart_passage.faceview.FaceResult;
import com.yunbiao.yb_smart_passage.faceview.FaceView;
import com.yunbiao.yb_smart_passage.utils.RestartAPPTool;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.xmpp.ServiceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private ScreenSaveFragment adsFragment;

    private View llMainTips;
    private PropertyValuesHolder animY;
    private ObjectAnimator objectAnimator;
    private CircleImageView ivHeadTips;
    private TextView tvNameTips;
    private TextView tvMainTips;

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
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    goSetting();
                }
                return true;
            }
        });
        if (faceView != null) {
            faceView.setCallback(faceCallback);
        }
        ivMainLogo = findViewById(R.id.iv_main_logo);
        tvMainAbbName = findViewById(R.id.tv_main_abbname);

        llMainTips = find(R.id.ll_main_tips);
        ivHeadTips = llMainTips.findViewById(R.id.iv_head_tips);
        tvMainTips = find(R.id.tv_main_tips);
        tvNameTips = find(R.id.tv_name_main_tips);

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
        }, 5 * 1000);
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
            if (mCurrFaceId != faceId) {
                mCurrFaceId = faceId;
                showMyTips(CHECK_ING);
            }
            showFaceLoading();
        }

        @Override
        public void onFaceVerify(VerifyResult verifyResult) {
            if (isAlwayOpen()) {
                return;
            }
            PassageManager.instance().checkSign(verifyResult);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(IntroduceUpdateEvent updateEvent) {
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
            showMyTips(passageBean.getName(), CHECK_SUCC,passageBean.getHeadPath());
            openDoor();
        }

        @Override
        public void onVerifyFailed() {
            showMyTips(CHECK_FAILED);
        }
    };

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

    private void setCompInfo() {
        String compName = SpUtils.getStr(SpUtils.COMPANY_NAME);
        String logoPath = SpUtils.getStr(SpUtils.COMPANY_LOGO);

        if (!TextUtils.isEmpty(compName)) tvMainAbbName.setText(compName);

        if (!TextUtils.isEmpty(logoPath) && new File(logoPath).exists())
            bindImageView(logoPath, ivMainLogo);
    }

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
        if (faceView != null) {
            faceView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (faceView != null) {
            faceView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceView != null) {
            faceView.destory();
        }
        destoryXmpp();

        SyncManager.instance().destory();
        LocateManager.instance().destory();
    }

    /*==识别提示=======================================================================================================*/
    private String CHECK_ING = "正在检测，请稍等... ";
    private String CHECK_SUCC = "识别成功，请通过";
    private String CHECK_FAILED = "验证失败，请重试";

    private void showFaceLoading() {
        if (llMainTips.isShown()) {
            startAutoHideTips();
            return;
        }
        llMainTips.setVisibility(View.VISIBLE);
        startAnim(llMainTips, llMainTips.getMeasuredHeight(), 0, new Runnable() {
            @Override
            public void run() {
                showMyTips(CHECK_ING);
            }
        });
    }

    private void showMyTips(String tips){
        showMyTips("",tips,null);
    }
    private void showMyTips(final String name, final String tips, final String path) {
        llMainTips.post(new Runnable() {
            @Override
            public void run() {
                ivHeadTips.clearAnimation();
                if (TextUtils.equals(tips, CHECK_ING)) {//如果是正在检测就开始动画
                    ivHeadTips.setImageResource(R.mipmap.bg_face_frame);
                    RotateAnimation rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setRepeatMode(Animation.INFINITE);
                    rotateAnimation.setRepeatCount(Animation.INFINITE);
                    rotateAnimation.setInterpolator(new LinearInterpolator());
                    rotateAnimation.setDuration(2000);
                    ivHeadTips.startAnimation(rotateAnimation);
                    tvMainTips.setTextColor(Color.WHITE);
                } else if(TextUtils.equals(tips,CHECK_FAILED)){//如果是检测失败就设置为红色
                    ivHeadTips.setImageResource(R.mipmap.error_face_frame);
                    tvMainTips.setTextColor(Color.RED);
                } else {//如果是检测成功就绿色
                    tvMainTips.setTextColor(Color.GREEN);
                    Glide.with(WelComeActivity.this).load(path).asBitmap().into(ivHeadTips);
                }

                tvNameTips.setText(TextUtils.isEmpty(name) ? "" : name);
                tvMainTips.setText(TextUtils.isEmpty(tips) ? "" : tips);
            }
        });
    }

    private void startAutoHideTips() {
        llMainTips.removeCallbacks(hideRunnable);
        llMainTips.postDelayed(hideRunnable, 1500);
    }

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hideFaceLoading();
        }
    };

    private void hideFaceLoading() {
        ivHeadTips.clearAnimation();
        startAnim(llMainTips, 0, llMainTips.getMeasuredHeight(), new Runnable() {
            @Override
            public void run() {
                llMainTips.setVisibility(View.GONE);
                showMyTips("");
            }
        });
    }


    private void startAnim(final View view, int formY, int toY, final Runnable runnable) {
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return;
        }
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

}