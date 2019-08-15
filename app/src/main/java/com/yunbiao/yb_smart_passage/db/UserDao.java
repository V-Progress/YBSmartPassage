package com.yunbiao.yb_smart_passage.db;

import android.content.Context;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by Administrator on 2018/10/8.
 */

public class UserDao extends BaseDao<UserBean>{
    public UserDao(Context context) {
        super(context, UserBean.class);
    }

    // 删除user表中的一条数据
    public int deleteByFaceId(int faceId) {
        int result = 0;
        try {
            List<UserBean> userBeans = queryByFaceId(faceId);
            if(userBeans != null && userBeans.size() > 0){
                result = dao.delete(userBeans.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<UserBean> queryById(int id){
        return queryByInt("id",id);
    }

    // 根据ID取出用户信息
    public List<UserBean> queryByFaceId(int faceId) {
        return queryByInt("faceId",faceId);
    }

    //根据depart取出人脸信息
    public List<UserBean> queryByDepart(String depart) {
        return queryByString("depart",depart);
    }

    //根据depart取出人脸信息
    public List<UserBean> queryByDepartAndName(String depart, String name) {
        List<UserBean> mlist = null;
        try {
            mlist = dao.queryBuilder().where().eq("depart", depart).and().eq("name", name).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mlist;
    }
}
