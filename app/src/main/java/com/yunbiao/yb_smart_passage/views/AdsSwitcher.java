package com.yunbiao.yb_smart_passage.views;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_passage.utils.FileUtils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AdsSwitcher extends FrameLayout implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "AdsSwitcher";
    private ImageView imageView;
    private TextureVideoView textureVideoView;
    private int switchTime = 10;
    private Queue<File> dataQueue = new LinkedList<>();

    public AdsSwitcher(Context context) {
        super(context);
        init();
    }

    public AdsSwitcher( Context context,  AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdsSwitcher( Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView,new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        textureVideoView = new TextureVideoView(getContext());
        addView(textureVideoView,new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        textureVideoView.setOnCompletionListener(this);
        textureVideoView.setOnErrorListener(this);

        textureVideoView.setBackgroundColor(Color.BLACK);
        imageView.setBackgroundColor(Color.BLACK);
        setBackgroundColor(Color.BLACK);

        //首先隐藏三个界面，待加载完成后再决定显示不显示
        setVisibility(View.INVISIBLE);
    }

    /***
     * 设置切换时间
     * @param time
     */
    public void setSwitchTime(int time){
        if(time >1){//如果大于1再设置，不允许设置小于1的数字
            switchTime = time;
        }
    }

    //设置数据
    public void setData(List<File> list){
        //首先清除数据
        clearData();

        if(list != null && list.size() > 0){
            Iterator<File> iterator = list.iterator();
            while (iterator.hasNext()) {
                File next = iterator.next();
                if(!next.exists()){
                    iterator.remove();
                }
            }
        }

        if(list == null || list.size() <= 0){
            return;
        }
        //置标识为true
        //添加所有数据进入循环队列
        dataQueue.addAll(list);
        if(!isShown()){
            setVisibility(View.VISIBLE);//显示界面
        }
        //开始自动计时
        justStartAutoPlay();
    }

    public void clearData(){
        if(isShown()){
            setVisibility(View.INVISIBLE);//隐藏页面
        }
        //清除数据
        stopAutoPlay();//停止计时
        dataQueue.clear();//清除数据队列
    }

    /***
     * 立刻开始计时（无延时）
     */
    public void justStartAutoPlay(){
        removeCallbacks(switchRunnable);
        post(switchRunnable);
    }

    /***
     * 停止计时
     */
    public void stopAutoPlay(){
        textureVideoView.stopPlayback();
        imageView.setImageBitmap(null);
        removeCallbacks(switchRunnable);
    }

    /***
     * 开始自动计时（有延时）
     */
    private void autoPlay(){
        removeCallbacks(switchRunnable);
        postDelayed(switchRunnable,switchTime * 1000);
    }

    //切换资源逻辑
    private Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            if(dataQueue.size() <= 0){
                return;
            }

            File poll = dataQueue.poll();
            dataQueue.offer(poll);
            if(poll == null){
                return;
            }

            Log.e(TAG, "run: " + poll.getPath());

            if (FileUtils.isVideo(poll.getPath())) {
                textureVideoView.setVideoPath(poll.getPath());
                textureVideoView.start();
                textureVideoView.setVisibility(View.VISIBLE);
                return;
            } else {
                Glide.with(getContext()).load(poll).skipMemoryCache(true).crossFade(500).into(imageView);
                textureVideoView.setVisibility(View.INVISIBLE);
            }

            if(dataQueue.size() <= 1){
                return;
            }
            autoPlay();
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onCompletion: 222222222222222");
        justStartAutoPlay();//播放完毕后立即播放下一个
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: 111111111111111");
        justStartAutoPlay();//播放失败后立即播放下一个
        return true;
    }
}
