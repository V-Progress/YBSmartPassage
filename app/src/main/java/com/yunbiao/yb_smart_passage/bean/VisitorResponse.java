package com.yunbiao.yb_smart_passage.bean;

import com.yunbiao.yb_smart_passage.db2.VisitorBean;

import java.util.List;

public class VisitorResponse {
    private String message;
    private int status;
    private List<VisitorBean> visitor;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<VisitorBean> getVisitor() {
        return visitor;
    }

    public void setVisitor(List<VisitorBean> visitor) {
        this.visitor = visitor;
    }

    @Override
    public String toString() {
        return "VisitorResponse{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", visitor=" + visitor +
                '}';
    }
}
