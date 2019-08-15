package com.yunbiao.yb_smart_passage.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Administrator on 2018/10/10.
 */
@DatabaseTable(tableName = "PassageBean")
public class PassageBean {

    @DatabaseField(generatedId = true)
    private int id;

    public int getId() {
        return id;
    }

    @DatabaseField(columnName = "departName")
    private String departName;

    @DatabaseField(columnName = "faceId")
    private int faceId;

    @DatabaseField(columnName = "similar")
    private String similar;

    @DatabaseField(columnName = "headPath")
    private String headPath;

    @DatabaseField(columnName = "isPass")
    private int isPass;

    @DatabaseField(columnName = "entryId")
    private int entryId;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "passTime")
    private long passTime;

    @DatabaseField(columnName = "isUpload")
    private boolean isUpload = false;

    @DatabaseField(columnName = "card")
    private String card;

    @DatabaseField(columnName = "sex")
    private int sex;

    @DatabaseField(columnName = "date")
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public PassageBean() {
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public long getPassTime() {
        return passTime;
    }

    public void setPassTime(long passTime) {
        this.passTime = passTime;
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

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PassageBean{" +
                "departName='" + departName + '\'' +
                ", faceId='" + faceId + '\'' +
                ", similar='" + similar + '\'' +
                ", headPath='" + headPath + '\'' +
                ", isPass=" + isPass +
                ", entryId='" + entryId + '\'' +
                ", name='" + name + '\'' +
                ", passTime=" + passTime +
                ", isUpload=" + isUpload +
                '}';
    }
}
