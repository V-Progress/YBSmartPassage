package com.yunbiao.yb_smart_passage.db;

import android.content.Context;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class SignDao extends BaseDao<PassageBean>{

    private static final String TAG = "SignDao";

    public SignDao(Context context) {
        super(context, PassageBean.class);
    }

    //根据年月日取出签到信息
    public List<PassageBean> queryByDate(String date) {
        List<PassageBean> mlist = null;
        try {
            mlist = dao.queryBuilder().orderBy("id", false).where().eq("date", date).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }

    //根据是否上传取出签到信息
    public List<PassageBean> queryByIsUpload(boolean isUpload) {
        List<PassageBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("isUpload", isUpload).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
