package com.yunbiao.yb_smart_passage.bean;

import com.yunbiao.yb_smart_passage.db2.UserBean;

import java.util.List;

/**
 * Created by Administrator on 2018/10/18.
 */

public class StaffBean {

    private List<DepEntry> dep;
    private int status;

    public class DepEntry{
        int depId;
        String depName;
        List<com.yunbiao.yb_smart_passage.db2.UserBean> entry;

        public int getDepId() {
            return depId;
        }

        public void setDepId(int depId) {
            this.depId = depId;
        }

        public String getDepName() {
            return depName;
        }

        public void setDepName(String depName) {
            this.depName = depName;
        }

        public List<UserBean> getEntry() {
            return entry;
        }

        public void setEntry(List<UserBean> entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return "DepEntry{" +
                    "depId=" + depId +
                    ", depName='" + depName + '\'' +
                    ", entry=" + entry +
                    '}';
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<DepEntry> getDep() {
        return dep;
    }

    public void setDep(List<DepEntry> dep) {
        this.dep = dep;
    }

    @Override
    public String toString() {
        return "StaffBean{" +
                "dep=" + dep +
                ", status=" + status +
                '}';
    }
}
