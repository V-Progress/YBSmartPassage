package com.yunbiao.yb_smart_passage.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.Event.SysInfoUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.afinel.Constants;
import com.yunbiao.yb_smart_passage.system.CoreInfoHandler;
import com.yunbiao.yb_smart_passage.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SystemActivity extends BaseActivity implements View.OnClickListener {

    private Button btn_depart_system;
    private Button btn_add_system;
    private Button btn_data_system;
    private Button btn_setting_system;
    private TextView tv_company_system;
    private TextView tv_deviceno_system;
    private TextView tv_exp_system;
    private TextView tv_server_system;
    private View ivBack;
    private TextView tv_bindcode_syetem;
    private TextView tv_online_system;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_system_h;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_system_h;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_depart_system = (Button) findViewById(R.id.btn_depart_system);
        btn_add_system = (Button) findViewById(R.id.btn_add_system);
        btn_data_system = (Button) findViewById(R.id.btn_data_system);
        btn_setting_system = (Button) findViewById(R.id.btn_setting_system);

        tv_deviceno_system = (TextView) findViewById(R.id.tv_deviceno_system);
        tv_bindcode_syetem = (TextView) findViewById(R.id.tv_bindcode_syetem);
        tv_company_system = (TextView) findViewById(R.id.tv_company_system);
        tv_exp_system = (TextView) findViewById(R.id.tv_exp_system);
        tv_server_system = (TextView) findViewById(R.id.tv_server_system);
        tv_online_system = (TextView) findViewById(R.id.tv_online_system);

        btn_depart_system.setOnClickListener(this);
        btn_add_system.setOnClickListener(this);
        btn_data_system.setOnClickListener(this);
        btn_setting_system.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(SysInfoUpdateEvent updateEvent) {
        d("update: ----- 收到系统信息更新事件");
        setInfo();
    }

    public void setInfo() {
        String serNum = SpUtils.getStr(SpUtils.DEVICE_NUMBER);
        tv_deviceno_system.setText(serNum);

        String bindCode = SpUtils.getStr(SpUtils.BINDCODE);
        tv_bindcode_syetem.setText(bindCode);

        String companyName = SpUtils.getStr(SpUtils.COMPANY_NAME);
        tv_company_system.setText(companyName);

        String host = Constants.RESOURCE_URL;
        tv_server_system.setText("云服务");
        if (host.contains("192.168.")) {
            tv_server_system.setText("本地服务");
        }

        String expDate = SpUtils.getStr(SpUtils.EXP_DATE);
        tv_exp_system.setText(TextUtils.isEmpty(expDate) ? "无限期" : expDate);

        tv_online_system.setText(CoreInfoHandler.isOnline ? "在线" : "离线");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_depart_system:
                startActivity(new Intent(this, EmployListActivity.class));
                break;
            case R.id.btn_add_system:
                startActivity(new Intent(this, AddEmployActivity.class));
                break;
            case R.id.btn_data_system:
                startActivity(new Intent(this, SignActivity.class));
                break;
            case R.id.btn_setting_system:
                startActivity(new Intent(this,SettingActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

