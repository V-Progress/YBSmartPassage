package com.yunbiao.yb_smart_passage.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.bean.BaseResponse;
import com.yunbiao.yb_smart_passage.db2.DaoManager;
import com.yunbiao.yb_smart_passage.db2.DepartBean;
import com.yunbiao.yb_smart_passage.db2.UserBean;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

public class EditInfoActivity extends BaseActivity {
    private static final String TAG = "EditInfoActivity";
    public static final String TYPE_KEY = "type";
    public static final String USER_KEY = "faceId";
    public static final int TYPE_ADD = 0;
    public static final int TYPE_UPDATE = 1;

    private int mCurrType;
    private RadioGroup rgSex;
    private EditText etName;
    private EditText etJob;
    private EditText etBirth;
    private EditText etSignature;
    private ImageView ivCapture;

    //正在编辑的员工信息
    private UserBean userBean;
    //当前头像路径
    private String mCurrHeadPath;
    //当前部门ID
    private long mCurrDepartId;

    private EditText etNumber;
    private Spinner spnDepart;
    private Button btnSubmit;

    @Override
    protected String setTitle() {
        return "员工信息";
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_edit_info;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_edit_info;
    }

    @Override
    protected void initView() {
        ivCapture = find(R.id.iv_capture);
        etName = find(R.id.et_name);
        rgSex = find(R.id.rg_sex);
        etJob = find(R.id.et_job);
        etBirth = find(R.id.et_birth);
        spnDepart = find(R.id.spn_depart);
        etSignature = find(R.id.et_tips);
        etNumber = find(R.id.et_number);
        btnSubmit = find(R.id.btn_submit);

        Drawable drawable = getResources().getDrawable(R.drawable.shape_spinner_drop);
        spnDepart.setPopupBackgroundDrawable(drawable);
        spnDepart.setOnItemSelectedListener(onItemSelectedListener);
    }

    @Override
    protected void initData() {
        //加载部门列表
        loadDepart();

        //获取操作类型
        mCurrType = getIntent().getIntExtra(TYPE_KEY, TYPE_ADD);
        Log.e(TAG, "initData: ---- " + mCurrType);
        if (mCurrType == TYPE_UPDATE) {
            String faceId = getIntent().getStringExtra(USER_KEY);
            if (TextUtils.isEmpty(faceId)) {
                UIUtils.showShort(this, "员工信息不存在");
                finish();
                return;
            }
            List<UserBean> userBeans = DaoManager.get().queryUserByFaceId(faceId);
            if (userBeans == null || userBeans.size() <= 0) {
                UIUtils.showShort(this, "员工信息不存在");
                finish();
                return;
            }
            userBean = userBeans.get(0);
            Log.e(TAG, "initData: ----- " + userBean.toString());
            setUserInfo(userBean);
        } else {
            rgSex.check(R.id.rb_male);
            spnDepart.setSelection(0);
        }
    }

    private void setUserInfo(UserBean userInfo) {
        //姓名
        etName.setText(userInfo.getName());
        //性别
        rgSex.check(userInfo.getSex() == 1 ? R.id.rb_male : R.id.rb_female);
        //编号
        etNumber.setText(userInfo.getNumber());
        //职位
        etJob.setText(userInfo.getPosition());
        //生日
        etBirth.setText(userInfo.getBirthday());
        //设置部门列表
        mCurrDepartId = userInfo.getDepartId();
        selectDepart(mCurrDepartId);
        //签名
        etSignature.setText(userInfo.getAutograph());
        //头像
        String headPath = userInfo.getHeadPath();
        mCurrHeadPath = headPath;
        Glide.with(this).load(headPath).asBitmap().error(R.mipmap.error_face_frame).into(ivCapture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                String imagePath = data.getStringExtra("ImagePath");
                mCurrHeadPath = imagePath;
                Glide.with(this).load(imagePath).asBitmap().error(R.mipmap.error_face_frame).into(ivCapture);
            }
        }
    }

    public void goTakePhoto(View view) {
        startActivityForResult(new Intent(this, CaptureActivity.class), 111);
    }

    public void onConfirm(View view) {
        String name = etName.getText().toString();
        int sex = rgSex.getCheckedRadioButtonId() == R.id.rb_male ? 1 : 0;
        String number = etNumber.getText().toString();
        String job = etJob.getText().toString();
        String birth = etBirth.getText().toString();
        String signature = etSignature.getText().toString();

        Map<String, String> params = new HashMap<>();
        params.put("depId", mCurrDepartId + "");

        String url;
        if (mCurrType == TYPE_ADD) {
            if (TextUtils.isEmpty(mCurrHeadPath)) {
                UIUtils.showShort(this, "请先拍照");
                return;
            }
            if(!new File(mCurrHeadPath).exists()){
                UIUtils.showShort(this, "获取头像失败,请重试");
                return;
            }
            if (TextUtils.isEmpty(name)) {
                UIUtils.showShort(this, "请填写姓名");
                return;
            }
            if (TextUtils.isEmpty(number)) {
                UIUtils.showShort(this, "请填写编号");
                return;
            }
            //必传
            params.put("comId", SpUtils.getInt(SpUtils.COMPANY_ID) + "");
            params.put("name", name);
            params.put("sex", sex + "");
            params.put("number", number);
            //非必传
            if (!TextUtils.isEmpty(job)) params.put("position", job);
            if (!TextUtils.isEmpty(birth)) params.put("birthday", job);
            if (!TextUtils.isEmpty(signature)) params.put("autograph", job);
            url = ResourceUpdate.ADDSTAFF;
        } else {
            params.put("id", userBean.getId()+"");
            if(!TextUtils.isEmpty(name) && !TextUtils.equals(name,userBean.getName())){
                params.put("name", name);
            }
            if(!TextUtils.isEmpty(number) && !TextUtils.equals(number,userBean.getNumber())){
                params.put("number", number);
            }
            if(!TextUtils.isEmpty(job) && !TextUtils.equals(job,userBean.getPosition())){
                params.put("position", job);
            }
            if(!TextUtils.isEmpty(signature) && !TextUtils.equals(signature,userBean.getAutograph())){
                params.put("autograph", job);
            }
            url = ResourceUpdate.UPDATSTAFF;
        }

        requestEdit(url,params,mCurrHeadPath);
    }

    private void requestEdit(String url,Map<String, String> params, String headPath) {
        btnSubmit.setEnabled(false);

        PostFormBuilder builder = OkHttpUtils.post().url(url).params(params);
        File file = new File(headPath);
        if (!TextUtils.isEmpty(headPath) && file.exists()) {
            builder.addFile("head", file.getName(), file);
            params.put("headName", file.getName());
        }

        d("地址--- " + ResourceUpdate.ADDSTAFF);
        d("参数--- " + params.toString());
        d("文件--- " + file.getName() + "," + file.getPath());

        builder.build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                showNetLoading(EditInfoActivity.this);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                d("添加失败："  +(e  == null ? "null" : e.getMessage()));
                UIUtils.showNoCheck(EditInfoActivity.this, "失败，请重试");
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "onResponse: " + Thread.currentThread().getName());
                d("添加结果：" + response);
                BaseResponse baseResponse = new Gson().fromJson(response, BaseResponse.class);
                if(baseResponse.getStatus() == 1){
                    finish();
                } else {
                    UIUtils.showNoCheck(EditInfoActivity.this, "失败，请重试");
                }
            }

            @Override
            public void onAfter(int id) {
                dismissNetLoading();
                btnSubmit.setEnabled(true);
            }
        });
    }

    public void onCancel(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(EditInfoActivity.this);
        builder.setTitle("提示！");
        builder.setMessage("确定放弃修改吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private List<DepartBean> departBeans = new ArrayList<>();
    private SpnAdapter spnAdapter;
    private void loadDepart() {
        List<DepartBean> list = DaoManager.get().queryAll(DepartBean.class);
        departBeans.addAll(list);
        spnAdapter = new SpnAdapter(this.departBeans);
        spnDepart.setAdapter(spnAdapter);
    }
    private int selectDepart(long departId) {
        for (int i = 0; i < departBeans.size(); i++) {
            DepartBean departBean = departBeans.get(i);
            if (departBean.getDepId() == departId) {
                return i;
            }
        }
        return 0;
    }
    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            DepartBean item = spnAdapter.getItem(position);
            mCurrDepartId = item.getDepId();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private class SpnAdapter extends BaseAdapter {
        List<DepartBean> datas;

        public SpnAdapter(List<DepartBean> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public DepartBean getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextColor(getResources().getColor(R.color.font_green));
            tv.setPadding(20, 10, 20, 10);
            tv.setTextSize(26);
            DepartBean departBean = datas.get(position);
            tv.setText(departBean.getDepName());
            convertView = tv;
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        onCancel(null);
    }

    private static Dialog dialog;
    public static void showNetLoading(Context context){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        AVLoadingIndicatorView avLoadingIndicatorView = new AVLoadingIndicatorView(context);
        avLoadingIndicatorView.setBackgroundColor(Color.TRANSPARENT);
        frameLayout.addView(avLoadingIndicatorView,new FrameLayout.LayoutParams(300,200, Gravity.CENTER));
        dialog.setContentView(frameLayout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void dismissNetLoading(){
        if(dialog != null){
            dialog.dismiss();
        }
    }
}
