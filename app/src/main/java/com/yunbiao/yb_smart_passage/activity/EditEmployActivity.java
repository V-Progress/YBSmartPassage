package com.yunbiao.yb_smart_passage.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.db.UserBean;
import com.yunbiao.yb_smart_passage.db.UserDao;
import com.yunbiao.yb_smart_passage.faceview.FaceView;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by Administrator on 2018/8/7.
 */

public class EditEmployActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EditEmployActivity";
    private static final int REQUEST_CODE_1 = 0x001;

    private Button btn_submit;
    private Button btn_TakePhoto;
    private Button btn_ReTakePhoto;
    private Button btn_cancle;
    private ImageView iv_back;

    //    private FaceCanvasView mFaceOverlay;
    private ImageView iv_capture;

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private EditText et_name;
    private EditText et_num;
    private EditText et_job;

    private String strFileAdd;
    private UserDao userDao;
    private MediaPlayer shootMP;
    private View pbTakePhoto;
    private Bitmap currFaceBitmap = null;
    private View faceFrame;

    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";
    private FaceView faceView;
    private int faceId;
    private RadioGroup rgSex;
    private CheckBox cbStat;
    private EditText etTips;
    private UserBean userBean;

    private int sex = 1;
    private int isStat = 0;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_editemploy_h;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_editemploy_h;
    }

    @Override
    protected void initView() {
        faceView = findViewById(R.id.face_view);
        et_name = findViewById(R.id.et_name);
        rgSex = findViewById(R.id.rg_sex);
        et_num = findViewById(R.id.et_num);
        et_job = findViewById(R.id.et_job);
        cbStat = findViewById(R.id.cb_stat);
        etTips = findViewById(R.id.et_tips);

        btn_submit = findViewById(R.id.btn_submit);
        iv_capture = findViewById(R.id.iv_capture);
        btn_TakePhoto = findViewById(R.id.btn_TakePhoto);
        btn_cancle = findViewById(R.id.btn_cancle);
        btn_ReTakePhoto = findViewById(R.id.btn_ReTakePhoto);
        iv_back = findViewById(R.id.iv_back);
        pbTakePhoto = findViewById(R.id.alv_take_photo);

        btn_TakePhoto.setOnClickListener(this);
        btn_ReTakePhoto.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        userDao = APP.getUserDao();
        faceId = getIntent().getIntExtra("faceId",-1);
        if(faceId == -1){
            UIUtils.showLong(this,"获取数据失败，请重试");
            finish();
            return;
        }

        List<UserBean> userBeans = userDao.queryByFaceId(faceId);
        if(userBeans == null || userBeans.size() <= 0){
            UIUtils.showLong(this,"获取数据失败，请重试");
            finish();
            return;
        }

        userBean = userBeans.get(0);

//        isStat = userBean.getIsStat();
//
//        et_name.setText(userBean.getName());
//        rgSex.check(userBean.getSex() == 1 ? R.id.rb_male : R.id.rb_female);
//        et_num.setText(userBean.getEmpId() + "");
//        et_job.setText(userBean.getPosition());
//        cbStat.setChecked(userBean.getIsStat() == 1);
//        etTips.setText(userBean.getTips());
//        etTips.setHint(userBean.getIsStat() == 1 ? Constants.DEFALUT_LEADER_TIPS : Constants.DEFALUT_TIPS);
        Glide.with(this).load(userBean.getHeadPath()).skipMemoryCache(true).crossFade(300).into(iv_capture);

        rgSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_male) {
                    sex = 1;
                } else {
                    sex = 0;
                }
            }
        });

        cbStat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isStat = isChecked ? 1 : 0;
                etTips.setHint(isStat == 1 ? Constants.DEFALUT_LEADER_TIPS : Constants.DEFALUT_TIPS);
            }
        });
    }

    /**
     * 播放系统拍照声音
     */
    public void shootSound() {
        AudioManager meng = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_ALARM);

        if (volume != 0) {
            if (shootMP == null)
                shootMP = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (shootMP != null)
                shootMP.start();
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            byte[] faceImageBytes = faceView.getFaceImage();
            if(faceImageBytes == null || faceImageBytes.length<=0){
                handler.sendEmptyMessageDelayed(0,200);
                return;
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final Bitmap faceImage = BitmapFactory.decodeByteArray(faceImageBytes, 0, faceImageBytes.length, options);
            strFileAdd = saveBitmap(faceImage);
            iv_capture.setImageBitmap(faceImage);

            pbTakePhoto.setVisibility(View.GONE);
            btn_TakePhoto.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_TakePhoto:
                pbTakePhoto.setVisibility(View.VISIBLE);
                btn_TakePhoto.setVisibility(View.GONE);
                handler.sendEmptyMessage(0);
                break;
            case R.id.btn_ReTakePhoto:
                currFaceBitmap = null;
                pbTakePhoto.setVisibility(View.GONE);
                btn_TakePhoto.setVisibility(View.VISIBLE);
                iv_capture.setImageResource(R.mipmap.avatar);
                break;
            case R.id.btn_submit:
                final String name = et_name.getText().toString();
                final String job = et_job.getText().toString();
                final String empNum = et_num.getText().toString();
                String tips = etTips.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    UIUtils.showShort(this,"名字不能为空！");
                    return;
                }
                if (TextUtils.isEmpty(empNum)) {
                    UIUtils.showShort(this,"员工编号不能为空！");
                    return;
                }
                if (TextUtils.isEmpty(job)) {
                    UIUtils.showShort(this,"职位不能为空！");
                    return;
                }

                if(TextUtils.isEmpty(tips)){
                    if(isStat == 1){
                        tips = Constants.DEFALUT_LEADER_TIPS;
                    } else {
                        tips = Constants.DEFALUT_TIPS;
                    }
                }

                final Integer empId = Integer.valueOf(empNum);
//                if(TextUtils.equals(name,userBean.getName())
//                        && sex == userBean.getSex()
//                        && empId == userBean.getEmpId()
//                        && TextUtils.equals(job,userBean.getPosition())
//                        && isStat == userBean.getIsStat()
//                        && TextUtils.equals(tips,userBean.getTips())
//                        && TextUtils.isEmpty(strFileAdd)){
//                    UIUtils.showShort(this,"未做修改！");
//                    return;
//                }

                File imgFile = null;
                if (TextUtils.isEmpty(strFileAdd)) {
                    String path = Environment.getExternalStorageDirectory() + "";
                    imgFile = new File(path + "/1.txt");
                    try {
                        if (!imgFile.exists())
                            imgFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    imgFile = new File(strFileAdd);
                }

                Map<String, String> params = new HashMap<>();
                params.put("id", empNum);
                params.put("name", name + "");
                params.put("sex",sex+"");
                params.put("position", job);
                params.put("isStat", isStat + "");
                params.put("tips",tips);

                requestEdit(params,imgFile);
                break;
            case R.id.btn_cancle:
                finish();
                break;
            case R.id.iv_back:
                finish();
                break;

        }
    }

    private void requestEdit(final Map<String, String> params, File imgFile){
        d(params.toString());
        OkHttpUtils.post()
                .url(ResourceUpdate.UPDATSTAFF)
                .params(params)
                .addFile("head", imgFile.getName(), imgFile)
                .build()
                .execute(new StringCallback() {
                    boolean isSucc = false;
                    @Override
                    public void onBefore(Request request, int id) {
                        btn_submit.setEnabled(false);
                        UIUtils.showNetLoading(EditEmployActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showShort(EditEmployActivity.this,"添加失败：\n" + (e != null ? e.getMessage() : "NULL"));
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "editStaffInfo....." + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.getInt("status");
                            if (status != 1) {
                                String errMsg;
                                switch (status) {
                                    case 2://添加失败
                                        errMsg = "添加失败";
                                        break;
                                    case 3://员工不存在
                                        errMsg = "该员工不存在";
                                        break;
                                    case 6://部门不存在
                                        errMsg = "不存在该部门";
                                        break;
                                    case 7://不存在公司部门关系
                                        errMsg = "公司没有这个部门";
                                        break;
                                    case 8://不存在员工的公司部门信息
                                        errMsg = "公司没有这位员工";
                                        break;
                                    default://参数错误
                                        errMsg = "参数错误";
                                        break;
                                }
                                UIUtils.showShort(EditEmployActivity.this,"" + errMsg);
                                return;
                            }
                            isSucc = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        btn_submit.setEnabled(true);
                        UIUtils.dismissNetLoading();
                        if(isSucc){
                            EditEmployActivity.this.finish();
                        }
                    }
                });
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
        String savePath;
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
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceView.pause();
    }
}
