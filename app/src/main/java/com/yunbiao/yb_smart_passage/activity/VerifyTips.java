package com.yunbiao.yb_smart_passage.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_passage.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class VerifyTips {

    private static VerifyTips verifyTips = new VerifyTips();
    private Activity mAct;
    private TextView tvMainTips;
    private TextView tvNameTips;
    private CircleImageView ivHeadTips;
    private View llMainTips;
    private PropertyValuesHolder animY;
    private ObjectAnimator objectAnimator;

    public static String CHECK_ING = "正在检测，请稍等... ";
    public static String CHECK_SUCC = "识别成功，请通过";
    public static String CHECK_FAILED = "验证失败，请重试";

    public static VerifyTips instance() {
        return verifyTips;
    }

    /***
     * 初始化
     * @param activity
     */
    public void init(Activity activity) {
        mAct = activity;
        llMainTips = mAct.findViewById(R.id.ll_main_tips);
        ivHeadTips = mAct.findViewById(R.id.iv_head_tips);
        tvMainTips = mAct.findViewById(R.id.tv_main_tips);
        tvNameTips = mAct.findViewById(R.id.tv_name_main_tips);
    }

    /*==识别提示=======================================================================================================*/

    /***
     * 显示人脸加载框
     */
    public void showFaceLoading() {
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

    /***
     * 延迟显示我的提示
     */
    public void showMyTipsDelay() {
        llMainTips.removeCallbacks(showTipsRunnable);
        llMainTips.postDelayed(showTipsRunnable, 1500);
    }

    /***
     * 立刻显示我的提示
     * @param tips 提示内容
     */
    public void showMyTips(String tips) {
        showMyTips(0,"", tips, null);
    }

    /***
     * 显示我的提示
     * @param name 名称
     * @param tips 提示内容
     * @param path 头像路径
     */
    public void showMyTips(final int userType, final String name, final String tips, final String path) {
        llMainTips.post(new Runnable() {
            @Override
            public void run() {
                ivHeadTips.clearAnimation();
                if (TextUtils.equals(tips, CHECK_ING)) {//如果是正在检测就开始动画
                    ivHeadTips.setImageResource(R.mipmap.bg_face_frame);
                    RotateAnimation rotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setRepeatMode(Animation.INFINITE);
                    rotateAnimation.setRepeatCount(Animation.INFINITE);
//                    rotateAnimation.setInterpolator(new LinearInterpolator());
                    rotateAnimation.setDuration(2000);
                    ivHeadTips.startAnimation(rotateAnimation);
                    tvMainTips.setTextColor(Color.WHITE);
                    tvNameTips.setText("");
                } else if (TextUtils.equals(tips, CHECK_FAILED)) {//如果是检测失败就设置为红色
                    ivHeadTips.setImageResource(R.mipmap.error_face_frame);
                    tvMainTips.setTextColor(Color.RED);
                    tvNameTips.setText("");
                } else {//如果是检测成功就绿色
                    tvMainTips.setTextColor(Color.GREEN);
                    Glide.with(mAct).load(path).asBitmap().into(ivHeadTips);

                    if (userType == -1) {
                        tvNameTips.setText(TextUtils.isEmpty(name) ? "访客" : "访客   "+name);
                    } else {
                        tvNameTips.setText(TextUtils.isEmpty(name) ? "员工" : "员工   "+name);
                    }
                }


                tvMainTips.setText(TextUtils.isEmpty(tips) ? "" : tips);
            }
        });
    }

    /*
    * 隐藏人脸加载框
    * */
    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hideFaceLoading();
        }
    };

    /*
     * 显示我的提示
     * */
    private Runnable showTipsRunnable = new Runnable() {
        @Override
        public void run() {
            showMyTips(CHECK_ING);
        }
    };

    /*
     * 开始自动隐藏提示的计时
     * */
    private void startAutoHideTips() {
        llMainTips.removeCallbacks(hideRunnable);
        llMainTips.postDelayed(hideRunnable, 1500);
    }

    /*
     * 隐藏人脸加载框的具体方法
     * */
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
