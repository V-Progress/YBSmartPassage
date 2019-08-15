package com.yunbiao.yb_smart_passage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_passage.activity.fragment.ResourceResolver;
import com.yunbiao.yb_smart_passage.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class InfoViewPager extends NoPreloadViewPager {

    private List<ResourceResolver.IntroBean.ResourceBean> mList = new ArrayList<>();
    private BannerViewAdapter bannerViewAdapter;
    private AutoSwitchTask autoSwitchTask;

    private ImageView mImageView;
    private VideoView mVideoView;

    boolean mIsAutoPlay = true;
    long mAutoPlayTime = 5;

    private int mTotalNum = 0;
    private int mCurrNum = 1;

    private boolean isAdsOpened = false;

    public InfoViewPager(Context context) {
        super(context);
        init();
    }

    public InfoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    PageChangeListener pageChangeListener;
    public void setPageChangeListener(PageChangeListener listener){
        pageChangeListener = listener;
    }

    public interface PageChangeListener{
        void onPageChange(int currPosi,int totalNum);
    }

    private static final String TAG = "InfoViewPager";

    private void init(){
        autoSwitchTask = new AutoSwitchTask();
        setOffscreenPageLimit(0);
        bannerViewAdapter = new BannerViewAdapter();
        setAdapter(bannerViewAdapter);

        setOnPageChangeListener(onPageChangeListener);
    }

    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if(getRealCount() == 0){
                if(pageChangeListener != null){
                    pageChangeListener.onPageChange(0,0);
                }
                return;
            }
            mCurrNum = position % getRealCount() + 1;
            if(pageChangeListener != null){
                pageChangeListener.onPageChange(mCurrNum,mTotalNum);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public void setData(List<ResourceResolver.IntroBean.ResourceBean> pathList, int delayTime) {
        mTotalNum = 0;
        mCurrNum = 1;
        if(pageChangeListener != null){
            pageChangeListener.onPageChange(mCurrNum,mTotalNum);
        }
        if (pathList == null || pathList.size() <= 0) {
            mList.clear();
            if(bannerViewAdapter != null){
                bannerViewAdapter.notifyDataSetChanged();
            }
            return;
        }
        if(delayTime != 0){
            mAutoPlayTime = delayTime;
        }

        mList.clear();
        mList.addAll(pathList);
        bannerViewAdapter = new BannerViewAdapter();
        setAdapter(bannerViewAdapter);
        mTotalNum = mList.size();
        if(pageChangeListener != null){
            pageChangeListener.onPageChange(mCurrNum,mTotalNum);
        }
        startAutoPlay();
    }

    class BannerViewAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            if(getRealCount() == 0){
                return null;
            }

            View view = null;
            int realPosition = position % getRealCount();
            ResourceResolver.IntroBean.ResourceBean resourceBean = mList.get(realPosition);
            String path = resourceBean.getPath();
            if(FileUtils.isVideo(path)){
                FrameLayout frameLayout = new FrameLayout(container.getContext());
                mVideoView = new VideoView(getContext());
                mVideoView.setFocusable(true);
                mVideoView.setVideoURI(Uri.parse(path));
                //开始播放
                mVideoView.start();
                mVideoView.setOnCompletionListener(onCompletionListener);
                mVideoView.setOnErrorListener(onErrorListener);
                frameLayout.addView(mVideoView,new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));
                view = frameLayout;
            } else {
                mImageView = new ImageView(getContext());
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Glide.with(getContext()).load(path)
                        .skipMemoryCache(true)
                        .into(mImageView);
                view = mImageView;
            }
            container.addView(view,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if(mImageView != null){
                releaseImageViewResouce(mImageView);
            }
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mIsAutoPlay ? Integer.MAX_VALUE : mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap=null;
            }
        }
        System.gc();
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            nextPlay();
        }
    };

    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            nextPlay();
            return true;
        }
    };

    public int getRealCount(){
        return mList.size();
    }

    private void nextPlay(){
        //判断广告已开启则不跳转
        if(isAdsOpened){
            d( "广告已开启... 不跳转下一个");
            return;
        }
        stopAutoPlay();
        if (mIsAutoPlay) {
            d( "开始下一个... ");
            postDelayed(autoSwitchTask, 0);
        }
    }

    public void startAutoPlay() {
        stopAutoPlay();
        if (mIsAutoPlay) {
            d( "开始自动轮播... ");
            postDelayed(autoSwitchTask, mAutoPlayTime * 1000);
        }
    }

    public void stopAutoPlay() {
        if (autoSwitchTask != null) {
            d( "停止自动轮播... ");
            removeCallbacks(autoSwitchTask);
        }
    }

    public void onAdsOpened(){
        isAdsOpened = true;
        //广告开启时判断当前是否是视频，如果是的话就暂停视频
        if(mVideoView != null){
            d( "广告开启... 暂停视频");
            mVideoView.pause();
        }
        stopAutoPlay();
    }

    public void onAdsClosed(){
        isAdsOpened = false;
        //广告关闭时判断当前是否是视频，如果是的话则开启视频
        if(mVideoView != null){
            d( "广告关闭... 开始视频");
            mVideoView.start();
        }
        startAutoPlay();
    }

    private class AutoSwitchTask implements Runnable {
        private AutoSwitchTask() {
        }
        @Override
        public void run() {
            //自动轮播的时候判断如果有视频正在播放，则不继续执行（播放完后会自动跳转下一个）
            if(mVideoView != null && mVideoView.isPlaying()){
                d( "正在播放视频... 不跳转" );
                return;
            }
            int currentItem = getCurrentItem() + 1;
            d( getCurrentItem() + " 切换到：" + currentItem);
            setCurrentItem(currentItem);
            startAutoPlay();
        }
    }
    private void d(String msg){
        Log.d(TAG, msg);
    }
}
