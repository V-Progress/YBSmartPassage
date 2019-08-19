package com.yunbiao.yb_smart_passage.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yunbiao.yb_smart_passage.R;
import com.yunbiao.yb_smart_passage.db2.PassageBean;

import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by Administrator on 2018/9/17.
 */

public class SignAdapter extends BaseAdapter {


    private static final String TAG = "SignAdapter";
    private Context context;
    private List<PassageBean> mlist;

    public SignAdapter(Context context, List<PassageBean> mlist) {
        this.context = context;
        this.mlist = mlist;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    @Override
    public PassageBean getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
       ViewHolder viewHolder=null;
        if (convertView == null){
            convertView = View.inflate(context, R.layout.item_sign,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        PassageBean passageBean=mlist.get(position);
        viewHolder.tv_No.setText(position+1+"");

        viewHolder.tv_date.setText(passageBean.getPassTime()+"");

        if (!TextUtils.isEmpty(passageBean.getName())){
            viewHolder.tv_employName.setText(passageBean.getName());
        }

        if (passageBean.getUserType() == 0) {
            viewHolder.tv_employJob.setText("员工");
            viewHolder.tv_employJob.setTextColor(Color.WHITE);
        } else {
            viewHolder.tv_employJob.setText("访客");
            viewHolder.tv_employJob.setTextColor(Color.RED);
        }


        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        viewHolder.tv_date.setText(df.format(passageBean.getPassTime()));
        if(passageBean.isUpload()){
            if (position%2==1){
                convertView.setBackgroundColor(Color.parseColor("#132841"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#112135"));
            }
        } else {
            convertView.setBackgroundColor(Color.parseColor("#A8DB8400"));
        }

        if (!TextUtils.isEmpty(passageBean.getHeadPath())){
            x.image().bind(viewHolder.iv_photo,passageBean.getHeadPath());
        }

        return convertView;
    }

    class ViewHolder{
        View root;
        TextView tv_No;
        TextView tv_date;
        TextView tv_employName;
        TextView tv_employJob;
        TextView tv_edit;
        ImageView iv_photo;


        public ViewHolder(View convertView) {
            root = convertView.findViewById(R.id.layout_title);
            tv_No= (TextView) convertView.findViewById(R.id.tv_No);
            tv_date= (TextView) convertView.findViewById(R.id.tv_date);
            tv_employName= (TextView) convertView.findViewById(R.id.tv_employName);
            tv_employJob= (TextView) convertView.findViewById(R.id.tv_employJob);
            tv_edit= (TextView) convertView.findViewById(R.id.tv_edit);
            iv_photo= (ImageView) convertView.findViewById(R.id.iv_photo);

        }
    }
}
