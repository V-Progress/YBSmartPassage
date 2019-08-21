package com.yunbiao.yb_smart_passage.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.faceview.FaceView;
import com.yunbiao.yb_smart_passage.utils.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class CaptureActivity extends BaseActivity {

    private View alvLoading;
    private Button btnTake;
    private ImageView ivCapture;
    private FaceView faceView;
    private Handler takeHandler = new Handler();
    private View llPreview;
    private Button btnOk;
    private Button btnRetry;

    @Override
    protected String setTitle() {
        return null;
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_capture;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_capture;
    }

    @Override
    protected void initView() {
        faceView = find(R.id.face_view);
        ivCapture = find(R.id.iv_capture);
        btnTake = find(R.id.btn_TakePhoto);
        alvLoading = find(R.id.alv_take_photo);
        llPreview = find(R.id.ll_preview);
        btnRetry = find(R.id.btn_retry);
        btnOk = find(R.id.btn_ok);
    }

    public void takePhoto(View view){
        btnTake.setVisibility(View.GONE);
        btnTake.setEnabled(false);
        alvLoading.setVisibility(View.VISIBLE);
        startCaptureHandler();
    }

    private void startCaptureHandler(){
        takeHandler.removeCallbacks(runnable);
        takeHandler.post(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            byte[] faceImage = faceView.getFaceImage();
            if(faceImage != null && faceImage.length > 0){
                btnTake.setEnabled(true);
                btnTake.setVisibility(View.VISIBLE);
                alvLoading.setVisibility(View.GONE);

                showPreview(faceImage);
                return;
            }
            takeHandler.postDelayed(runnable,300);
        }
    };

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
    }

    private void showPreview(final byte[] imgBytes){
        llPreview.setVisibility(View.VISIBLE);
        Glide.with(CaptureActivity.this).load(imgBytes).asBitmap().into(ivCapture);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llPreview.setVisibility(View.GONE);
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBitmap(imgBytes, new SaveImgListener() {
                    @Override
                    public void onFailed() {
                        UIUtils.showShort(CaptureActivity.this,"保存失败，请重试");
                    }

                    @Override
                    public void onSuccess(String path) {
                        Intent intent = new Intent();
                        intent.putExtra("ImagePath",path);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                });
            }
        });

    }

    public static String SCREEN_BASE_PATH = Constants.CACHE_PATH;
    /**
     * 保存bitmap到本地
     *
     * @return
     */
    public static void saveBitmap(final byte[] bitmapByte, final SaveImgListener saveImgListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap  bitmap= BitmapFactory.decodeByteArray(bitmapByte , 0, bitmapByte.length);
                File filePic;
                try {
                    //格式化时间
                    long time = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String sdfTime = sdf.format(time);
                    filePic = new File(SCREEN_BASE_PATH + sdfTime + ".jpg");
                    if (!filePic.exists()) {
                        filePic.getParentFile().mkdirs();
                        filePic.createNewFile();
                    }
                    // TODO: 2019/3/28 闪退问题
                    FileOutputStream fos = new FileOutputStream(filePic);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    // TODO: 2019/3/28 闪退问题
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    if(saveImgListener != null){
                        saveImgListener.onFailed();
                    }
                    e.printStackTrace();
                    return;
                }

                if(saveImgListener != null){
                    saveImgListener.onSuccess(filePic.getPath());
                }
            }
        }).start();
    }

    interface SaveImgListener{
        void onFailed();
        void onSuccess(String path);
    }
}
