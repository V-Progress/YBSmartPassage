package com.yunbiao.yb_smart_passage.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "CompBean")
public class CompBean implements Serializable {
    private String message;
    private int status;

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "comid",unique = true)
    private int comid;//名字

    @DatabaseField(columnName = "compName")
    private String compName;//名字

    @DatabaseField(columnName = "iconPath")
    private String iconPath;

    @DatabaseField(columnName = "iconUrl")
    private String iconUrl;

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getComid() {
        return comid;
    }

    public void setComid(int comid) {
        this.comid = comid;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    @Override
    public String toString() {
        return "CompBean{" +
                "id=" + id +
                ", comid=" + comid +
                ", compName='" + compName + '\'' +
                ", iconPath='" + iconPath + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }
}
