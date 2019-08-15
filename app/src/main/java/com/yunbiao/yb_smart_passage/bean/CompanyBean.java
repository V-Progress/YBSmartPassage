package com.yunbiao.yb_smart_passage.bean;

import java.util.List;

/**
 * Created by Administrator on 2018/10/18.
 */

public class CompanyBean {

    /**
     * company : {"gotime":"09:30","downtime":"15:00","downtips":"成功","late":[{"lateNum":1,"entryId":11}],"gotips":"成功","devicePwd":123456,"comlogo":"http://192.168.1.54/imgserver/resource/logo/2018/2018-10-09/510f9119-84a6-4f5b-8879-679de19e1ac0.jpg","comname":"云标","comid":15,"deparray":[{"sondep":[{"sondepId":4,"sondepName":"财务部"}],"depId":1,"depName":"技术部"},{"depId":3,"depName":"人事办"}]}
     * status : 5
     */
    private CompanyEntity company;
    private int status;

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public int getStatus() {
        return status;
    }

    public class CompanyEntity {
        @Override
        public String toString() {
            return "CompanyEntity{" +
                    "gotime='" + gotime + '\'' +
                    ", downtime='" + downtime + '\'' +
                    ", downtips='" + downtips + '\'' +
                    ", late=" + late +
                    ", gotips='" + gotips + '\'' +
                    ", devicePwd='" + devicePwd + '\'' +
                    ", comlogo='" + comlogo + '\'' +
                    ", comname='" + comname + '\'' +
                    ", abbname='" + abbname + '\'' +
                    ", notice='" + notice + '\'' +
                    ", toptitle='" + toptitle + '\'' +
                    ", bottomtitle='" + bottomtitle + '\'' +
                    ", themeid=" + themeid +
                    ", comid=" + comid +
                    ", deparray=" + deparray +
                    '}';
        }

        /**
         * gotime : 09:30
         * downtime : 15:00
         * downtips : 成功
         * late : [{"lateNum":1,"entryId":11}]
         * gotips : 成功
         * devicePwd : 123456
         * comlogo : http://192.168.1.54/imgserver/resource/logo/2018/2018-10-09/510f9119-84a6-4f5b-8879-679de19e1ac0.jpg
         * comname : 云标
         * comid : 15
         * deparray : [{"sondep":[{"sondepId":4,"sondepName":"财务部"}],"depId":1,"depName":"技术部"},{"depId":3,"depName":"人事办"}]
         */
        private String gotime;
        private String downtime;
        private String downtips;
        private List<LateEntity> late;
        private String gotips;
        private String devicePwd;
        private String comlogo;
        private String comname;
        private String abbname;
        private String notice;
        private String toptitle;
        private String bottomtitle;
        private int themeid;
        private int comid;
        private List<DeparrayEntity> deparray;

        public String getNotice() {
            return notice;
        }

        public void setNotice(String notice) {
            this.notice = notice;
        }

        public String getToptitle() {
            return toptitle;
        }

        public void setToptitle(String toptitle) {
            this.toptitle = toptitle;
        }

        public String getBottomtitle() {
            return bottomtitle;
        }

        public void setBottomtitle(String bottomtitle) {
            this.bottomtitle = bottomtitle;
        }

        public int getThemeid() {
            return themeid;
        }

        public void setThemeid(int themeid) {
            this.themeid = themeid;
        }

        public String getAbbname() {
            return abbname;
        }

        public void setAbbname(String abbname) {
            this.abbname = abbname;
        }

        public void setGotime(String gotime) {
            this.gotime = gotime;
        }

        public void setDowntime(String downtime) {
            this.downtime = downtime;
        }

        public void setDowntips(String downtips) {
            this.downtips = downtips;
        }

        public void setLate(List<LateEntity> late) {
            this.late = late;
        }

        public void setGotips(String gotips) {
            this.gotips = gotips;
        }

        public void setDevicePwd(String devicePwd) {
            this.devicePwd = devicePwd;
        }

        public void setComlogo(String comlogo) {
            this.comlogo = comlogo;
        }

        public void setComname(String comname) {
            this.comname = comname;
        }

        public void setComid(int comid) {
            this.comid = comid;
        }

        public void setDeparray(List<DeparrayEntity> deparray) {
            this.deparray = deparray;
        }

        public String getGotime() {
            return gotime;
        }

        public String getDowntime() {
            return downtime;
        }

        public String getDowntips() {
            return downtips;
        }

        public List<LateEntity> getLate() {
            return late;
        }

        public String getGotips() {
            return gotips;
        }

        public String getDevicePwd() {
            return devicePwd;
        }

        public String getComlogo() {
            return comlogo;
        }

        public String getComname() {
            return comname;
        }

        public int getComid() {
            return comid;
        }

        public List<DeparrayEntity> getDeparray() {
            return deparray;
        }

        public class LateEntity {
            /**
             * lateNum : 1
             * entryId : 11
             */
            private int lateNum;
            private int entryId;

            public void setLateNum(int lateNum) {
                this.lateNum = lateNum;
            }

            public void setEntryId(int entryId) {
                this.entryId = entryId;
            }

            public int getLateNum() {
                return lateNum;
            }

            public int getEntryId() {
                return entryId;
            }

            @Override
            public String toString() {
                return "LateEntity{" +
                        "lateNum=" + lateNum +
                        ", entryId=" + entryId +
                        '}';
            }
        }

        public class DeparrayEntity {
            /**
             * sondep : [{"sondepId":4,"sondepName":"财务部"}]
             * depId : 1
             * depName : 技术部
             */
            private int depId;
            private String depName;

            public void setDepId(int depId) {
                this.depId = depId;
            }

            public void setDepName(String depName) {
                this.depName = depName;
            }

            public int getDepId() {
                return depId;
            }

            public String getDepName() {
                return depName;
            }

            public class SondepEntity {
                /**
                 * sondepId : 4
                 * sondepName : 财务部
                 */
                private int sondepId;
                private String sondepName;

                public void setSondepId(int sondepId) {
                    this.sondepId = sondepId;
                }

                public void setSondepName(String sondepName) {
                    this.sondepName = sondepName;
                }

                public int getSondepId() {
                    return sondepId;
                }

                public String getSondepName() {
                    return sondepName;
                }
            }

            @Override
            public String toString() {
                return "DeparrayEntity{" +
                        "depId=" + depId +
                        ", depName='" + depName + '\'' +
                        '}';
            }
        }

    }

    @Override
    public String toString() {
        return "CompanyBean{" +
                "company=" + company +
                ", status=" + status +
                '}';
    }
}
