package com.yunbiao.yb_smart_passage.db2;

/**
 * Created by Administrator on 2017/7/28.
 */

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

@Entity
public class DepartBean {

    @Id
    protected Long id;
    @Unique
    protected long depId;
    protected String depName;

    @Transient
    private List<UserBean> entry;

    @Generated(hash = 1223813775)
    public DepartBean(Long id, long depId, String depName) {
        this.id = id;
        this.depId = depId;
        this.depName = depName;
    }

    @Generated(hash = 790678164)
    public DepartBean() {
    }

    @Override
    public String toString() {
        return "DepartBean{" +
                "depId=" + depId +
                ", depName='" + depName + '\'' +
                ", entry=" + entry +
                '}';
    }

    public List<UserBean> getEntry() {
        return entry;
    }

    public void setEntry(List<UserBean> entry) {
        this.entry = entry;
    }

    public long getDepId() {
        return depId;
    }

    public void setDepId(long depId) {
        this.depId = depId;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
