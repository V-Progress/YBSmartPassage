package com.yunbiao.yb_smart_passage.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.yunbiao.yb_smart_passage.R;

import java.util.Timer;
import java.util.TimerTask;

public class LoopPicture extends ImageView{
    private Context mContext;
    private ImageView breathImageView;
    private Timer timer;
    private boolean isOpen = true;
    private int index = 0;
    private final int BREATH_INTERVAL_TIME = 3000; //设置呼吸灯时间间隔
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    breathImageView.clearAnimation();
                    breathImageView.setAnimation(getFadeIn());
                    break;
                case 2:
                    breathImageView.clearAnimation();
                    breathImageView.setAnimation(getFadeOut());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public LoopPicture(Context context) {
        super(context);
        this.mContext=context;
        breathImageView=this;
        startTimer();
    }
    public LoopPicture(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        breathImageView=this;
        startTimer();
    }

    public LoopPicture(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        breathImageView=this;
        startTimer();
    }

    private Animation getFadeIn() {
        Animation fadeIn = AnimationUtils.loadAnimation(mContext,
                R.anim.anim_breathing_in);
        fadeIn.setDuration(BREATH_INTERVAL_TIME);
        fadeIn.setStartOffset(100);
        return fadeIn;
    }

    private Animation getFadeOut() {
        Animation fadeOut = AnimationUtils.loadAnimation(mContext,
                R.anim.anim_breathing_out);
        fadeOut.setDuration(BREATH_INTERVAL_TIME);
        fadeOut.setStartOffset(100);
        return fadeOut;
    }

    private void startTimer() {
        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (isOpen) {
                    if (index == 2) {
                        index = 0;
                    }
                    index++;
                    Message message = new Message();
                    message.what = index;
                    handler.sendMessage(message);
                }
            }
        };
        timer.schedule(task, 0, BREATH_INTERVAL_TIME); // 延时0ms后执行，5000ms执行一次
    }
}