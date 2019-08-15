package com.yunbiao.yb_smart_passage.bean;

import com.yunbiao.yb_smart_passage.db.UserBean;

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
        List<UserBean> entry;

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

//        public class Entry{
//            private String autograph;
//            private String birthday;
//            private String cardId;
//            private int faceId;
//            private String head;
//            private String name;
//            private String number;
//            private String position;
//            private int id;
//            private int sex;
//
//            public String getAutograph() {
//                return autograph;
//            }
//
//            public void setAutograph(String autograph) {
//                this.autograph = autograph;
//            }
//
//            public String getBirthday() {
//                return birthday;
//            }
//
//            public void setBirthday(String birthday) {
//                this.birthday = birthday;
//            }
//
//            public String getCardId() {
//                return cardId;
//            }
//
//            public void setCardId(String cardId) {
//                this.cardId = cardId;
//            }
//
//            public int getFaceId() {
//                return faceId;
//            }
//
//            public void setFaceId(int faceId) {
//                this.faceId = faceId;
//            }
//
//            public String getHead() {
//                return head;
//            }
//
//            public void setHead(String head) {
//                this.head = head;
//            }
//
//            public String getName() {
//                return name;
//            }
//
//            public void setName(String name) {
//                this.name = name;
//            }
//
//            public String getNumber() {
//                return number;
//            }
//
//            public void setNumber(String number) {
//                this.number = number;
//            }
//
//            public String getPosition() {
//                return position;
//            }
//
//            public void setPosition(String position) {
//                this.position = position;
//            }
//
//            public int getId() {
//                return id;
//            }
//
//            public void setId(int id) {
//                this.id = id;
//            }
//
//            public int getSex() {
//                return sex;
//            }
//
//            public void setSex(int sex) {
//                this.sex = sex;
//            }
//
//            @Override
//            public String toString() {
//                return "Entry{" +
//                        "autograph='" + autograph + '\'' +
//                        ", birthday='" + birthday + '\'' +
//                        ", cardId='" + cardId + '\'' +
//                        ", faceId='" + faceId + '\'' +
//                        ", head='" + head + '\'' +
//                        ", name='" + name + '\'' +
//                        ", number='" + number + '\'' +
//                        ", position='" + position + '\'' +
//                        ", id=" + id +
//                        ", sex=" + sex +
//                        '}';
//            }
//        }
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
