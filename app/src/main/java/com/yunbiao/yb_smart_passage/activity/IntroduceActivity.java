package com.yunbiao.yb_smart_passage.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.activity.fragment.ResourceResolver;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.yunbiao.yb_smart_passage.views.InfoViewPager;

import java.util.ArrayList;
import java.util.List;

public class IntroduceActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "IntroduceActivity";
    private ListView lvBtn;
    private List<ResourceResolver.IntroBean> introBeans = new ArrayList<>();
    private BtnAdapter btnAdapter;
    private View pbIntro;
    private View ivLeft;
    private ResourceResolver resourceResolver;
    private String cacheData;
    private View flIntro;
    private WebView webView;
    private InfoViewPager infoViewPager;
    private int mCurrPosition = -1;
    private TextView tvIndicator;

    @Override
    protected int getPortraitLayout() {
        return R.layout.fragment_introduce;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.fragment_introduce;
    }

    @Override
    protected void initView() {
        lvBtn = findViewById(R.id.lv_information_button);
        pbIntro = findViewById(R.id.ll_progress_info);
        ivLeft = findViewById(R.id.iv_information_right);
        flIntro = findViewById(R.id.fl_intro);
        webView = findViewById(R.id.wv_web);
        infoViewPager = findViewById(R.id.ivp);
        tvIndicator = findViewById(R.id.tv_indicator);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initData() {
        initWebView();
        ivLeft.setOnClickListener(this);
        infoViewPager.setOnTouchListener(this);
        webView.setOnTouchListener(this);
        lvBtn.setOnTouchListener(this);
        ivLeft.setOnClickListener(this);

        btnAdapter = new BtnAdapter(introBeans);
        lvBtn.setAdapter(btnAdapter);
        lvBtn.setOnItemClickListener(onItemClickListener);
        infoViewPager.setPageChangeListener(new InfoViewPager.PageChangeListener() {
            @Override
            public void onPageChange(int currPosi, int totalNum) {
                Log.e(TAG, "onPageChange: " + currPosi + " --- " + totalNum);
                tvIndicator.setText(currPosi + " / " + totalNum);
            }
        });

        initIntroData();
        startAutoClose();
    }

    private void initIntroData(){
        ResourceResolver.instance().init();
        ResourceResolver.instance().setListener(new ResourceResolver.DownloadListener() {
            @Override
            public void onResolveBefore() {
                pbIntro.setVisibility(View.VISIBLE);
                mCurrPosition = -1;
                introBeans.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onResolveSuccess(ResourceResolver.IntroBean introBean) {
                d("onSuccess: " + introBean.toString());
                introBeans.add(introBean);
                p(lvBtn,new Runnable() {
                    @Override
                    public void run() {
                        btnAdapter.notifyDataSetChanged();
                        if(introBeans.size() > 0 && mCurrPosition == -1){
                            clickItem(0);
                        }
                    }
                });
            }

            @Override
            public void onResolveFinish(List<ResourceResolver.IntroBean> introList) {
                pd(lvBtn,1500,new Runnable() {
                    @Override
                    public void run() {
                        btnAdapter.notifyDataSetChanged();
                        pbIntro.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void noResource() {
                Log.e(TAG, "onResponse: 1111111111111111111111111111");
                pbIntro.setVisibility(View.GONE);
                introBeans.clear();
                btnAdapter.notifyDataSetChanged();
                infoViewPager.setData(null,0);
                UIUtils.showShort(IntroduceActivity.this,"暂无宣传数据");
                finish();
            }
        });
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.e(TAG, "onItemClick: ----- " + position);
            closeBtnList();
            clickItem(position);
        }
    };

    private void clickItem(int position){
        if(position == mCurrPosition){
            return;
        }

        Log.e(TAG, "clickItem: ----- " + lvBtn.getChildCount());
        for (int i = 0; i < lvBtn.getChildCount(); i++) {
            View childAt = lvBtn.getChildAt(i);
            if(i == position){
                childAt.setBackgroundResource(R.mipmap.bg_intro_btn_click);
            } else {
                childAt.setBackgroundResource(R.mipmap.bg_intro_btn);
            }
        }

        mCurrPosition = position;
        ResourceResolver.IntroBean introBean = introBeans.get(position);
        if(introBean.getType() == 2){
            infoViewPager.setVisibility(View.GONE);
            tvIndicator.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(introBean.getUrl());
            return;
        }

        webView.setVisibility(View.GONE);
        infoViewPager.setVisibility(View.VISIBLE);
        tvIndicator.setVisibility(View.VISIBLE);

        infoViewPager.setData(introBean.getResList(),introBean.getTime());
        infoViewPager.startAutoPlay();
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可

        //支持插件
        settings.setPluginState(WebSettings.PluginState.ON);
        //设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); //默认缓存
        settings.setAllowFileAccess(true); //设置可以访问文件
        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
        settings.setDefaultTextEncodingName("utf-8");//设置编码格式

        webView.setWebViewClient(new WebViewClient() {
            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (lvBtn.isShown()) {
            closeBtnList();
        } else {
            openBtnList();
        }
    }

    private void openBtnList() {
        if (!lvBtn.isShown()) {
            lvBtn.setVisibility(View.VISIBLE);
            ivLeft.setVisibility(View.GONE);
            startAnim(200, lvBtn, -lvBtn.getMeasuredWidth(), 0, null);
        }
    }

    private void closeBtnList() {
        if (lvBtn.isShown()) {
            startAnim(100, lvBtn, 0, -lvBtn.getMeasuredWidth(), new Runnable() {
                @Override
                public void run() {
                    ivLeft.setVisibility(View.VISIBLE);
                    lvBtn.setVisibility(View.GONE);
                }
            });
        }
    }

    private ObjectAnimator objectAnimator;
    private PropertyValuesHolder animX;

    private void startAnim(int duration, final View view, float fromX, float toX, final Runnable runnable) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);//开始动画前开启硬件加速
        animX = PropertyValuesHolder.ofFloat("translationX", fromX, toX);//生成值动画
        //加载动画Holder
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, animX);
        objectAnimator.setDuration(duration);
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

    private void p(View view,Runnable runnable){
        pd(view,0,runnable);
    }

    private void pd(View view,int delay,Runnable runnable){
        view.postDelayed(runnable,delay);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            startAutoClose();
        }
        return false;
    }

    private void startAutoClose(){
        ivLeft.removeCallbacks(runnable);
        ivLeft.postDelayed(runnable,15 * 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    class BtnAdapter extends BaseAdapter {
        private List<ResourceResolver.IntroBean> mList;

        public BtnAdapter(List<ResourceResolver.IntroBean> propaList) {
            mList = propaList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public ResourceResolver.IntroBean getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new BtnAdapter.ViewHolder();
                convertView = View.inflate(parent.getContext(), R.layout.item_info_btn, null);
                viewHolder.tvBtn= convertView.findViewById(R.id.tv_btn);
                viewHolder.ivBtn= convertView.findViewById(R.id.iv_btn);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ResourceResolver.IntroBean introBean = mList.get(position);
            viewHolder.tvBtn.setText(introBean.getName());

            String path = introBean.getLogo().getPath();
            Glide.with(getActivity()).load(path).asBitmap().into(viewHolder.ivBtn);

            return convertView;
        }

        class ViewHolder {
            TextView tvBtn;
            ImageView ivBtn;
        }
    }

}
