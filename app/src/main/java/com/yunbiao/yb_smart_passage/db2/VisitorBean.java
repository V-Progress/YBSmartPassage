package com.yunbiao.yb_smart_passage.db2;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class VisitorBean{
    @Id
    protected long id;
    @Unique
    protected String faceId;

    protected String headPath;

    private int visComId;
    private String birthday;
    private int sex;
    private String phone;
    private int visDepartId;
    private String reason;
    private String visName;
    private String IdCard;
    private int type;
    private int currType;
    private String visDepartName;
    private int visEntryId;
    private String nation;
    private String unit;
    private String currEnd;
    private String name;
    private String head;
    private String currStart;

    private int addTag;

    @Generated(hash = 739366923)
    public VisitorBean(long id, String faceId, String headPath, int visComId,
            String birthday, int sex, String phone, int visDepartId, String reason,
            String visName, String IdCard, int type, int currType,
            String visDepartName, int visEntryId, String nation, String unit,
            String currEnd, String name, String head, String currStart,
            int addTag) {
        this.id = id;
        this.faceId = faceId;
        this.headPath = headPath;
        this.visComId = visComId;
        this.birthday = birthday;
        this.sex = sex;
        this.phone = phone;
        this.visDepartId = visDepartId;
        this.reason = reason;
        this.visName = visName;
        this.IdCard = IdCard;
        this.type = type;
        this.currType = currType;
        this.visDepartName = visDepartName;
        this.visEntryId = visEntryId;
        this.nation = nation;
        this.unit = unit;
        this.currEnd = currEnd;
        this.name = name;
        this.head = head;
        this.currStart = currStart;
        this.addTag = addTag;
    }

    @Generated(hash = 1877328823)
    public VisitorBean() {
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath) {
        this.headPath = headPath;
    }

    public int getAddTag() {
        return addTag;
    }

    public void setAddTag(int addTag) {
        this.addTag = addTag;
    }

    public int getVisComId() {
        return visComId;
    }

    public void setVisComId(int visComId) {
        this.visComId = visComId;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getVisDepartId() {
        return visDepartId;
    }

    public void setVisDepartId(int visDepartId) {
        this.visDepartId = visDepartId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getVisName() {
        return visName;
    }

    public void setVisName(String visName) {
        this.visName = visName;
    }

    public String getIdCard() {
        return IdCard;
    }

    public void setIdCard(String idCard) {
        IdCard = idCard;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCurrType() {
        return currType;
    }

    public void setCurrType(int currType) {
        this.currType = currType;
    }

    public String getVisDepartName() {
        return visDepartName;
    }

    public void setVisDepartName(String visDepartName) {
        this.visDepartName = visDepartName;
    }

    public int getVisEntryId() {
        return visEntryId;
    }

    public void setVisEntryId(int visEntryId) {
        this.visEntryId = visEntryId;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCurrEnd() {
        return currEnd;
    }

    public void setCurrEnd(String currEnd) {
        this.currEnd = currEnd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getCurrStart() {
        return currStart;
    }

    public void setCurrStart(String currStart) {
        this.currStart = currStart;
    }

    @Override
    public String toString() {
        return "VisitorBean{" +
                "id=" + id +
                ", visComId=" + visComId +
                ", birthday='" + birthday + '\'' +
                ", sex=" + sex +
                ", phone='" + phone + '\'' +
                ", faceId=" + faceId +
                ", visDepartId=" + visDepartId +
                ", reason='" + reason + '\'' +
                ", visName='" + visName + '\'' +
                ", IdCard='" + IdCard + '\'' +
                ", type=" + type +
                ", currType=" + currType +
                ", visDepartName='" + visDepartName + '\'' +
                ", visEntryId=" + visEntryId +
                ", nation='" + nation + '\'' +
                ", unit='" + unit + '\'' +
                ", currEnd='" + currEnd + '\'' +
                ", name='" + name + '\'' +
                ", head='" + head + '\'' +
                ", currStart='" + currStart + '\'' +
                ", headPath='" + headPath + '\'' +
                ", addTag=" + addTag +
                '}';
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFaceId() {
        return this.faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }
}
