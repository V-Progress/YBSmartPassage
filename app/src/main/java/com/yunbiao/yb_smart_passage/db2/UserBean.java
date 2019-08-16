package com.yunbiao.yb_smart_passage.db2;

/**
 * Created by Administrator on 2017/7/28.
 */

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class UserBean{

    @Id
    protected long id;
    @Unique
    protected String faceId;

    protected String headPath;

    private int downloadTag;
    private int departId;
    private String departName;
    private String autograph;
    private String birthday;
    private String cardId;
    private String head;
    private String name;

    private String number;

    private String position;

    private int sex;

    @Generated(hash = 100668937)
    public UserBean(long id, String faceId, String headPath, int downloadTag,
            int departId, String departName, String autograph, String birthday,
            String cardId, String head, String name, String number, String position,
            int sex) {
        this.id = id;
        this.faceId = faceId;
        this.headPath = headPath;
        this.downloadTag = downloadTag;
        this.departId = departId;
        this.departName = departName;
        this.autograph = autograph;
        this.birthday = birthday;
        this.cardId = cardId;
        this.head = head;
        this.name = name;
        this.number = number;
        this.position = position;
        this.sex = sex;
    }

    @Generated(hash = 1203313951)
    public UserBean() {
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

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHeadPath() {
        return this.headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public int getDownloadTag() {
        return this.downloadTag;
    }

    public void setDownloadTag(int downloadTag) {
        this.downloadTag = downloadTag;
    }

    public int getDepartId() {
        return this.departId;
    }

    public void setDepartId(int departId) {
        this.departId = departId;
    }

    public String getDepartName() {
        return this.departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getAutograph() {
        return this.autograph;
    }

    public void setAutograph(String autograph) {
        this.autograph = autograph;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getHead() {
        return this.head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getSex() {
        return this.sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getFaceId() {
        return this.faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }
}
