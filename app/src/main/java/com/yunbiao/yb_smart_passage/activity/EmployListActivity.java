package com.yunbiao.yb_smart_passage.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.yunbiao.yb_smart_passage.APP;
import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.adapter.EmployAdapter;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.business.SyncManager;
import com.yunbiao.yb_smart_passage.db.UserBean;
import com.yunbiao.yb_smart_passage.db.UserDao;
import com.yunbiao.yb_smart_passage.faceview.FaceSDK;
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

/**
 * Created by Administrator on 2018/8/7.
 */

public class EmployListActivity extends BaseActivity implements EmployAdapter.EmpOnDeleteListener,EmployAdapter.EmpOnEditListener,View.OnClickListener{

    private static final String TAG = "EmployListActivity";

    private ListView lv_employ_List;
    private EmployAdapter employAdapter;
    private List<UserBean> employList;
    private Button btn_addEmploy;
    private Button btn_sync;
    private ImageView iv_back;

    private UserDao userDao;

    private View rootView;
    private View avlLoading;

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
        lv_employ_List= (ListView) findViewById(R.id.lv_employ_List);
        btn_addEmploy= (Button) findViewById(R.id.btn_addEmploy);
        btn_sync= (Button) findViewById(R.id.btn_sync);
        iv_back= (ImageView) findViewById(R.id.iv_back);
        avlLoading = findViewById(R.id.avl_loading);

        btn_addEmploy.setOnClickListener(this);
        btn_sync.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        userDao=APP.getUserDao();

        employList=new ArrayList<>();
        employAdapter=new EmployAdapter(this,employList);
        employAdapter.setOnEmpDeleteListener(this);
        employAdapter.setOnEmpEditListener(this);
        lv_employ_List.setAdapter(employAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ininList();
    }

    //摄像头错误监听
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(EmployUpdate employUpdate) {
        ininList();
    }

    public static class EmployUpdate{

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void ininList() {
        avlLoading.setVisibility(View.VISIBLE);
        lv_employ_List.setVisibility(View.GONE);

        lv_employ_List.setVisibility(View.VISIBLE);
        avlLoading.setVisibility(View.GONE);

        employList.clear();
        List<UserBean> userBeans = userDao.selectAll();
        if (userBeans!=null){
            employList.addAll(userBeans);
            employAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void itemDeleteClick(View v,final int postion) {
        final UserBean userBean = employList.get(postion);
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployListActivity.this);

        //    设置Title的内容
        builder.setTitle("提示！");
        //    设置Content来显示一个信息
        builder.setMessage("确定删除吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final Map<String, String> map = new HashMap<String, String>();
//                map.put("entryId", userBean.getEmpId()+"");
                OkHttpUtils.post().url(ResourceUpdate.DELETESTAFF).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        UIUtils.showTitleTip(EmployListActivity.this,"删除失败 " + e != null?e.getMessage():"NULL");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        boolean b = FaceSDK.instance().removeUser(String.valueOf(userBean.getFaceId()));
                        if(b){
                            userDao.delete(employList.get(postion));
                            employList.remove(postion);
                            employAdapter.notifyDataSetChanged();
                            UIUtils.showTitleTip(EmployListActivity.this,"删除成功");
                        }
                    }
                });
            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //    显示出该对话框
        builder.show();
    }

    @Override
    public void itemEditClick(View v, final int postion) {
        //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployListActivity.this);

        //    设置Title的内容
        builder.setTitle("提示！");
        //    设置Content来显示一个信息
        builder.setMessage("确定去修改吗？");
        //    设置一个PositiveButton
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                UserBean userBean = employList.get(postion);
                Intent intent=new Intent(EmployListActivity.this, EditEmployActivity.class);
                intent.putExtra("faceId",userBean.getFaceId());
                startActivity(intent);

            }
        });
        //    设置一个NegativeButton
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        //    显示出该对话框
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_addEmploy:
                startActivity(new Intent(EmployListActivity.this, AddEmployActivity.class));
                break;
            case R.id.btn_sync:
                SyncManager.instance().initInfo();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
