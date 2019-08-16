package com.yunbiao.yb_smart_passage.db2;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;
import org.xutils.db.table.DbBase;

/**
 * Created by Administrator on 2018/10/10.
 */
@Entity
public class PassageBean {
    @Id
    protected Long id;

    private long entryId;

    protected String faceId;

    protected String headPath;

    private String departName;

    private String similar;
    private int isPass;
    private String name;
    private long passTime;
    private boolean isUpload = false;
    private String card;
    private int sex;
    private String createDate;

    private int userType;

    @Generated(hash = 715086318)
    public PassageBean(Long id, long entryId, String faceId, String headPath,
            String departName, String similar, int isPass, String name,
            long passTime, boolean isUpload, String card, int sex,
            String createDate, int userType) {
        this.id = id;
        this.entryId = entryId;
        this.faceId = faceId;
        this.headPath = headPath;
        this.departName = departName;
        this.similar = similar;
        this.isPass = isPass;
        this.name = name;
        this.passTime = passTime;
        this.isUpload = isUpload;
        this.card = card;
        this.sex = sex;
        this.createDate = createDate;
        this.userType = userType;
    }

    @Generated(hash = 1686457362)
    public PassageBean() {
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public Long getId() {
        return id;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getSimilar() {
        return similar;
    }

    public void setSimilar(String similar) {
        this.similar = similar;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public int getIsPass() {
        return isPass;
    }

    public void setIsPass(int isPass) {
        this.isPass = isPass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPassTime() {
        return passTime;
    }

    public void setPassTime(long passTime) {
        this.passTime = passTime;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public boolean getIsUpload() {
        return this.isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PassageBean{" +
                "id=" + id +
                ", entryId=" + entryId +
                ", faceId=" + faceId +
                ", headPath='" + headPath + '\'' +
                ", departName='" + departName + '\'' +
                ", similar='" + similar + '\'' +
                ", isPass=" + isPass +
                ", name='" + name + '\'' +
                ", passTime=" + passTime +
                ", isUpload=" + isUpload +
                ", card='" + card + '\'' +
                ", sex=" + sex +
                ", createDate='" + createDate + '\'' +
                ", userType=" + userType +
                '}';
    }

    public String getFaceId() {
        return this.faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }
}
