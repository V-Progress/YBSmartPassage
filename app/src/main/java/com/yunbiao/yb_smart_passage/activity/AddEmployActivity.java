package com.yunbiao.yb_smart_passage.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
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
import android.widget.Toast;

import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.db.UserDao;
import com.yunbiao.yb_smart_passage.faceview.FaceView;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Request;


/**
 * Created by Administrator on 2018/8/7.
 */

public class AddEmployActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "AddEmployActivity";
    private static final int REQUEST_CODE_1 = 0x001;

    private Button btn_submit;
    private Button btn_TakePhoto;
    private Button btn_ReTakePhoto;
    private Button btn_cancle;
    private ImageView iv_back;

    private ImageView iv_capture;

    private static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private EditText et_name;
    private EditText et_job;

    private String strFileAdd;
    private UserDao userDao;
    private MediaPlayer shootMP;
    private View pbTakePhoto;

    private FaceView faceView;
    private RadioGroup rgSex;
    private CheckBox cbStat;
    private EditText etTips;

    public static String SCREEN_BASE_PATH = sdPath + "/mnt/sdcard/photo/";

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_addemploy_h;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_addemploy_h;
    }

    @Override
    protected void initView() {
        faceView = findViewById(R.id.face_view);

        et_name = findViewById(R.id.et_name);
        rgSex = findViewById(R.id.rg_sex);
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

    private int sex = 1;
    private int isStat = 0;

    @Override
    protected void initData() {
        userDao = APP.getUserDao();

        strFileAdd = "";

        rgSex.check(sex == 1 ? R.id.rb_male : R.id.rb_female);
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

        cbStat.setChecked(isStat == 1);
        etTips.setHint((isStat == 1 ? Constants.DEFALUT_LEADER_TIPS : Constants.DEFALUT_TIPS) + "（默认值）");
        cbStat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isStat = isChecked ? 1 : 0;
                etTips.setHint((isStat == 1 ? Constants.DEFALUT_LEADER_TIPS : Constants.DEFALUT_TIPS) + "（默认值）");
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            byte[] faceImageBytes = faceView.getFaceImage();
            if (faceImageBytes == null || faceImageBytes.length <= 0) {
                handler.sendEmptyMessageDelayed(0, 200);
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
            case R.id.btn_TakePhoto://点击拍照
                pbTakePhoto.setVisibility(View.VISIBLE);
                btn_TakePhoto.setVisibility(View.GONE);
                handler.sendEmptyMessage(0);

                break;
            case R.id.btn_ReTakePhoto://重置所有状态
                pbTakePhoto.setVisibility(View.GONE);
                btn_TakePhoto.setVisibility(View.VISIBLE);
                iv_capture.setImageResource(R.mipmap.avatar);
                break;

            case R.id.btn_submit:
                final String name = et_name.getText().toString();
                final String job = et_job.getText().toString();
                String tips = etTips.getText().toString();

                if (TextUtils.isEmpty(strFileAdd)) {
                    UIUtils.showShort(this, "请先拍照！");
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    UIUtils.showShort(this, "名字不能为空！");
                    return;
                }
                if (TextUtils.isEmpty(job)) {
                    UIUtils.showShort(this, "职位不能为空！");
                    return;
                }

                if (TextUtils.isEmpty(tips)) {
                    if (isStat == 1) {
                        tips = Constants.DEFALUT_LEADER_TIPS;
                    } else {
                        tips = Constants.DEFALUT_TIPS;
                    }
                }

                // TODO: 2019/6/4
                String headName = strFileAdd.substring(strFileAdd.lastIndexOf("/") + 1);
                Log.e(TAG, "strFileAdd-----------------> " + strFileAdd);
                File imgFile = new File(strFileAdd);

                Map<String, String> params = new HashMap<>();
                params.put("name", name + "");
                params.put("sex", sex + "");
                params.put("headName", headName);
                params.put("position", job);
                int companyid = SpUtils.getInt(SpUtils.COMPANY_ID);
                params.put("comId", companyid + "");
                params.put("isStat", isStat + "");
                params.put("tips", tips);

                PostFormBuilder paramsBuilder = OkHttpUtils.post()
                        .url(ResourceUpdate.ADDSTAFF)
                        .params(params);
                if (imgFile != null && imgFile.exists()) {
                    paramsBuilder = paramsBuilder.addFile("head", imgFile.getName(), imgFile);
                }
                paramsBuilder.build().execute(new StringCallback() {
                    boolean isSucc = false;
                    @Override
                    public void onBefore(Request request, int id) {
                        btn_submit.setEnabled(false);
                        UIUtils.showNetLoading(AddEmployActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError------------------->" + (e != null ? e.getMessage() : e));
                        btn_submit.setEnabled(true);
                        UIUtils.showShort(AddEmployActivity.this, "连接服务器失败,请重新再试！\n（" + (e != null ? e.getMessage() : "NULL") + "）");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG, "result----------> " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getInt("status") == 1) {
                                isSucc = true;
                            } else if (jsonObject.getInt("status") == 7) {
                                UIUtils.showShort(AddEmployActivity.this, "部门不存在！");
                            } else {
                                UIUtils.showShort(AddEmployActivity.this, "提交失败，错误代码：" + jsonObject.getInt("status"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            btn_submit.setEnabled(true);
                            UIUtils.showShort(AddEmployActivity.this, "员工保存失败,请重新再试！(" + (e != null ? e.getMessage() : "NULL") + ")");
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        btn_submit.setEnabled(true);
                        UIUtils.dismissNetLoading();
                        if(isSucc){
                            finish();
                        }
                    }
                });
                break;
            case R.id.btn_cancle:
                finish();

                break;
            case R.id.iv_back:
                finish();
                break;

        }
    }

    /**
     * 随机生产文件名
     *
     * @return
     */
    private static String generateFileName() {
        return UUID.randomUUID().toString();
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(Bitmap mBitmap) {
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
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // TODO: 2019/3/28 闪退问题
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return filePic.getAbsolutePath();
    }


    private String getScreenHot(View v) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            v.draw(canvas);

            try {
                long time = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String sdfTime = sdf.format(time);
                String filePath = SCREEN_BASE_PATH + sdfTime + ".png";
                FileOutputStream fos = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Toast.makeText(AddEmployActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                System.out.println("保存成功");
                return filePath;
            } catch (FileNotFoundException e) {
                throw new InvalidParameterException();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        UIUtils.dismissNetLoading();
        super.onDestroy();
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
