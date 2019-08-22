package com.yunbiao.yb_smart_passage.db2;

import android.util.Log;

import com.yunbiao.yb_smart_passage.APP;

import org.greenrobot.greendao.database.Database;

import java.util.List;

public class DaoManager {
    private static final String TAG = "DaoManager";
    private static DaoManager daoManager = new DaoManager();
    private final String DB_NAME = "yb_passage_db";
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public static final long FAILURE = -1;
    public static final long SUCCESS = 0;

    public static DaoManager get(){
        return daoManager;
    }

    private DaoManager(){
    }

    public void initDb(){
        Log.e(TAG, "initDb: ");
        MySQLiteHelper helper =new MySQLiteHelper(APP.getContext(),DB_NAME,null);
        Log.e(TAG, "initDb: " + helper);
        Database db = helper.getWritableDb();
        Log.e(TAG, "initDb: " + db);
        daoMaster = new DaoMaster(db);
        Log.e(TAG, "initDb: " + daoMaster);
        daoSession = daoMaster.newSession();
        Log.e(TAG, "initDb: " + daoSession);
        daoSession.clear();
        daoSession.getVisitorBeanDao().detachAll();
        daoSession.getPassageBeanDao().detachAll();
        daoSession.getUserBeanDao().detachAll();
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public DaoMaster getDaoMaster(){
        return daoMaster;
    }

    public <T> long add(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
        return daoSession.insert(clazz);
    }

    public <T> long addOrUpdate(T clazz){
        if(daoSession == null){
            return FAILURE;
        }
       return  daoSession.insertOrReplace(clazz);
    }

    public <T>long update(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.update(t);
        return SUCCESS;
    }

    public <T>List<T> queryAll(Class<T> clazz){
        if(daoSession == null){
            return null;
        }
        return daoSession.loadAll(clazz);
    }

    public <T> List<T> queryById(Class<T> t, String id){
        if(daoSession == null){
            return null;
        }
        return daoSession.queryRaw(t,"id",id);
    }

    public List<UserBean> queryUserByFaceId(String faceId){
        if(daoSession == null){
            return null;
        }
        return daoSession.getUserBeanDao().queryBuilder().where(UserBeanDao.Properties.FaceId.eq(faceId)).list();
    }

    public List<DepartBean> queryDepartByCompId(int compId) {
        if(daoSession == null){
            return null;
        }
        return daoSession.getDepartBeanDao().queryBuilder().where(DepartBeanDao.Properties.CompId.eq(compId)).list();

    }

    public List<PassageBean> queryPassageByFaceId(String faceId){
        if(daoSession == null){
            return null;
        }
        return daoSession.getPassageBeanDao().queryBuilder().where(PassageBeanDao.Properties.FaceId.eq(faceId)).list();
    }

    public VisitorBean queryByVisitorFaceId(String faceId){
        if(daoSession == null){
            return null;
        }
        List<VisitorBean> visitorBeans = daoSession.getVisitorBeanDao().loadAll();
        for (VisitorBean visitorBean : visitorBeans) {
            Log.e(TAG, "queryByVisitorFaceId: ----- " + visitorBean.toString());
        }

        VisitorBeanDao visitorBeanDao = daoSession.getVisitorBeanDao();
        return visitorBeanDao.queryBuilder().where(VisitorBeanDao.Properties.FaceId.eq(faceId)).unique();
    }

    public List<PassageBean> queryByPassDate(String date){
        if(daoSession == null){
            return null;
        }
        return daoSession.getPassageBeanDao().queryBuilder().where(PassageBeanDao.Properties.CreateDate.eq(date)).list();
    }

    public List<PassageBean> queryByPassUpload(boolean b){
        if(daoSession == null){
            return null;
        }
        return daoSession.getPassageBeanDao().queryBuilder().where(PassageBeanDao.Properties.IsUpload.eq(b)).list();
    }

    public <T>long delete(T t){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.delete(t);
        return SUCCESS;
    }

    public <T>long deleteAll(Class<T> clazz){
        if(daoSession == null){
            return FAILURE;
        }
        daoSession.deleteAll(clazz);
        return SUCCESS;
    }

}
