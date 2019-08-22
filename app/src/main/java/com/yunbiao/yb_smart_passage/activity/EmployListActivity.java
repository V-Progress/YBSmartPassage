package com.yunbiao.yb_smart_passage.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.Event.IntroduceUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.adapter.EmployAdapter;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.business.SyncManager;
import com.yunbiao.yb_smart_passage.db2.DaoManager;
import com.yunbiao.yb_smart_passage.db2.DepartBean;
import com.yunbiao.yb_smart_passage.db2.UserBean;
import com.yunbiao.yb_smart_passage.faceview.FaceSDK;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.UIUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2018/8/7.
 */

public class EmployListActivity extends BaseActivity implements EmployAdapter.EmpOnDeleteListener, EmployAdapter.EmpOnEditListener, View.OnClickListener {

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private EmployAdapter employAdapter;
    private Button btn_addEmploy;
    private Button btn_sync;
    private ImageView iv_back;

    private View rootView;
    private View avlLoading;
    private Spinner spnDepart;

    private List<UserBean> allUsers;

    private List<UserBean> showList = new ArrayList<>();

    private long mCurrDepartId = 0;
    private List<DepartBean> allDeparts;
    private int mCompanyId;

    @Override
    protected String setTitle() {
        return "员工列表";
    }

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_employlist_h;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_employlist_h;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        rootView = findViewById(R.id.rl_root);
        lv_employ_List = (ListView) findViewById(R.id.lv_employ_List);
        btn_addEmploy = (Button) findViewById(R.id.btn_addEmploy);
        btn_sync = (Button) findViewById(R.id.btn_sync);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        avlLoading = findViewById(R.id.avl_loading);
        spnDepart = find(R.id.spn_depart);

        btn_addEmploy.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(EmployUpdate updateEvent) {
        initData();
    }

    @Override
    protected void initData() {
        mCompanyId = SpUtils.getInt(SpUtils.COMPANY_ID);

        initUserList();//初始化员工列表
        initAllDatas();//初始化所有数据
        loadDepart(allDeparts);//加载部门列表
    }

    //初始化用户表
    private void initUserList() {
        employAdapter = new EmployAdapter(this, showList);
        employAdapter.setOnEmpDeleteListener(this);
        employAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(employAdapter);
    }

    //初始化全部数据
    private void initAllDatas() {
        allDeparts = DaoManager.get().queryDepartByCompId(mCompanyId);//获取部门表
        allUsers = DaoManager.get().queryAll(UserBean.class);//获取用户表
        DepartBean departBean = new DepartBean();//在前面添加一个全部的选项
        departBean.setDepId(0);
        departBean.setDepName("全部");
        allDeparts.add(0, departBean);
    }

    //加载部门数据
    private void loadDepart(final List<DepartBean> allDeparts) {
        Drawable drawable = getResources().getDrawable(R.drawable.shape_spinner_drop);
        spnDepart.setPopupBackgroundDrawable(drawable);
        spnDepart.setAdapter(new SpnAdapter(allDeparts));
        spnDepart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DepartBean departBean = allDeparts.get(position);
                mCurrDepartId = departBean.getDepId();
                loadUser(mCurrDepartId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnDepart.setSelection(0);
    }

    //加载用户数据
    private void loadUser(long id) {
        avlLoading.setVisibility(View.VISIBLE);
        lv_employ_List.setVisibility(View.GONE);

        showList.clear();
        if (id <= 0) {
            showList.addAll(allUsers);
        } else {
            for (UserBean user : allUsers) {
                if (user.getDepartId() == id) {
                    showList.add(user);
                }
            }
        }
        employAdapter.notifyDataSetChanged();

        lv_employ_List.setVisibility(View.VISIBLE);
        avlLoading.setVisibility(View.GONE);
    }

    @Override
    public void itemDeleteClick(View v, final int postion) {
        final UserBean userBean = showList.get(postion);

        showDialog("确定删除吗？",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Map<String, String> map = new HashMap<>();
                map.put("entryId", userBean.getId() + "");
                OkHttpUtils.post().url(ResourceUpdate.DELETESTAFF).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        showNetLoading(EmployListActivity.this);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showTitleTip(EmployListActivity.this, "删除失败 " + e != null ? e.getMessage() : "NULL");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        boolean b = FaceSDK.instance().removeUser(userBean.getFaceId());
                        if (b) {
                            long delete = DaoManager.get().delete(userBean);
                            d("删除结果：" + delete);
                            showList.remove(userBean);
                            allUsers.remove(userBean);
                            employAdapter.notifyDataSetChanged();
                            UIUtils.showTitleTip(EmployListActivity.this, "删除成功");
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        dismissNetLoading();
                    }
                });
            }
        });
    }

    @Override
    public void itemEditClick(View v, final int postion) {
        showDialog("确定去修改吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserBean userBean = showList.get(postion);
                Log.e(TAG, "onClick: " + userBean.toString());
                Intent intent = new Intent(EmployListActivity.this, EditInfoActivity.class);
                intent.putExtra(EditInfoActivity.TYPE_KEY, EditInfoActivity.TYPE_UPDATE);
                intent.putExtra(EditInfoActivity.USER_KEY, userBean.getFaceId());
                startActivity(intent);
            }
        });
    }

    private void showDialog(String msg,DialogInterface.OnClickListener confirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示！");
        builder.setMessage(msg);
        builder.setPositiveButton("确定", confirm);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_addEmploy:
                Intent intent = new Intent(this, EditInfoActivity.class);
                intent.putExtra(EditInfoActivity.TYPE_KEY, EditInfoActivity.TYPE_ADD);
                startActivity(intent);
                break;
            case R.id.btn_sync:
                SyncManager.instance().initInfo();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    /***
     * ====================================================================================================
     */
    private static Dialog dialog;

    public static void showNetLoading(Context context) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.TRANSPARENT);
        AVLoadingIndicatorView avLoadingIndicatorView = new AVLoadingIndicatorView(context);
        avLoadingIndicatorView.setBackgroundColor(Color.TRANSPARENT);
        frameLayout.addView(avLoadingIndicatorView, new FrameLayout.LayoutParams(300, 200, Gravity.CENTER));
        dialog.setContentView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void dismissNetLoading() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

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
            tv.setTextSize(24);
            DepartBean departBean = datas.get(position);
            tv.setText(departBean.getDepName());
            convertView = tv;
            return convertView;
        }
    }

    public static class EmployUpdate{

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
