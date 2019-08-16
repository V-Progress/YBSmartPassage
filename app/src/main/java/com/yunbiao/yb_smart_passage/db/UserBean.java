package com.yunbiao.yb_smart_passage.db;

/**
 * Created by Administrator on 2017/7/28.
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "UserBean")
public class UserBean {

    @DatabaseField(columnName = "id", generatedId = true,unique = true)
    private int id;

    @DatabaseField(columnName = "headPath")
    private String headPath;

    @DatabaseField(columnName = "downloadTag")
    private int downloadTag;

    @DatabaseField(columnName = "departId")
    private int departId;
    @DatabaseField(columnName = "departName")
    private String departName;

    @DatabaseField(columnName = "autograph")
    private String autograph;
    @DatabaseField(columnName = "birthday")
    private String birthday;
    @DatabaseField(columnName = "cardId")
    private String cardId;
    @DatabaseField(columnName = "faceId")
    private int faceId;
    @DatabaseField(columnName = "head")
    private String head;
    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "number")
    private String number;

    @DatabaseField(columnName = "position")
    private String position;

    @DatabaseField(columnName = "sex")
    private int sex;

    public UserBean() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public int getDownloadTag() {
        return downloadTag;
    }

    public void setDownloadTag(int downloadTag) {
        this.downloadTag = downloadTag;
    }

    public int getDepartId() {
        return departId;
    }

    public void setDepartId(int departId) {
        this.departId = departId;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getAutograph() {
        return autograph;
    }

    public void setAutograph(String autograph) {
        this.autograph = autograph;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id=" + id +
                ", headPath='" + headPath + '\'' +
                ", downloadTag=" + downloadTag +
                ", departId=" + departId +
                ", departName='" + departName + '\'' +
                ", autograph='" + autograph + '\'' +
                ", birthday='" + birthday + '\'' +
                ", cardId='" + cardId + '\'' +
                ", faceId=" + faceId +
                ", head='" + head + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", position='" + position + '\'' +
                ", sex=" + sex +
                '}';
    }
}
