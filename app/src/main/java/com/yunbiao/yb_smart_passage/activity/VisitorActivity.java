package com.yunbiao.yb_smart_passage.activity;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.activity.Event.VisitorUpdateEvent;
import com.yunbiao.yb_smart_passage.activity.base.BaseActivity;
import com.yunbiao.yb_smart_passage.db2.DaoManager;
import com.yunbiao.yb_smart_passage.db2.VisitorBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.x;

import java.util.List;

public class VisitorActivity extends BaseActivity {

    private ListView lvVisitor;
    private View loading;

    @Override
    protected int getPortraitLayout() {
        return R.layout.activity_visitor;
    }

    @Override
    protected int getLandscapeLayout() {
        return R.layout.activity_visitor;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);

        lvVisitor = find(R.id.lv_employ_List);
        loading = find(R.id.avl_loading);
    }

    @Override
    protected void initData() {
        List<VisitorBean> visitorBeans = DaoManager.get().queryAll(VisitorBean.class);
        lvVisitor.setAdapter(new VisitorAdapter(visitorBeans));
        loading.setVisibility(View.GONE);
    }

    //摄像头错误监听
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(VisitorUpdateEvent event) {
        initData();
    }

    @Override
    protected String setTitle() {
        return "访客列表";
    }

    class VisitorAdapter extends BaseAdapter{
        private List<VisitorBean> list;
        public VisitorAdapter(List<VisitorBean> visitorBeans) {
            list = visitorBeans;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder = new ViewHolder();
                convertView = View.inflate(parent.getContext(),R.layout.item_visitor,null);
                viewHolder.root = convertView.findViewById(R.id.layout_title);
                viewHolder.tvName = convertView.findViewById(R.id.tv_name);
                viewHolder.tvComp = convertView.findViewById(R.id.tv_company);
                viewHolder.tvPhone = convertView.findViewById(R.id.tv_phone_number);
                viewHolder.tvVisit = convertView.findViewById(R.id.tv_visit_to);
                viewHolder.tvTime = convertView.findViewById(R.id.tv_visit_time);
                viewHolder.ivHead = convertView.findViewById(R.id.iv_visitor_head);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            VisitorBean visitorBean = list.get(position);

            viewHolder.tvName.setText(visitorBean.getName());
            viewHolder.tvComp.setText(visitorBean.getUnit());
            viewHolder.tvPhone.setText(visitorBean.getPhone());
            viewHolder.tvVisit.setText(visitorBean.getVisName());
            viewHolder.tvTime.setText(visitorBean.getCurrStart() + "\n~\n" + visitorBean.getCurrEnd());
            if(!TextUtils.isEmpty(visitorBean.getHeadPath())){
                x.image().bind(viewHolder.ivHead,visitorBean.getHeadPath());
            }

            if(visitorBean.getAddTag() != 0){
                viewHolder.root.setBackgroundColor(Color.RED);
            } else {
                if(position % 2 == 0){
                    viewHolder.root.setBackgroundColor(Color.parseColor("#0A204E"));
                } else {
                    viewHolder.root.setBackgroundColor(Color.parseColor("#1D3A59"));
                }
            }

            return convertView;
        }

        class ViewHolder{
            View root;
            TextView tvName;
            TextView tvComp;
            TextView tvPhone;
            TextView tvVisit;
            TextView tvTime;
            ImageView ivHead;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
