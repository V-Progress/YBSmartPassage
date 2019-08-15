package com.yunbiao.yb_smart_passage.bean;

import java.util.List;

public class IntroduceBean {
    int status;
    String message;
    List<Propa> propaArray;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Propa> getPropaArray() {
        return propaArray;
    }

    public void setPropaArray(List<Propa> propaArray) {
        this.propaArray = propaArray;
    }

    public class Propa{
        String descInfo;
        int id;
        int type;
        int time;
        String logo;
        String name;
        String url;
        List<String> imgArray;
        List<String> videoArray;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public List<String> getImgArray() {
            return imgArray;
        }

        public void setImgArray(List<String> imgArray) {
            this.imgArray = imgArray;
        }

        public List<String> getVideoArray() {
            return videoArray;
        }

        public void setVideoArray(List<String> videoArray) {
            this.videoArray = videoArray;
        }

        public String getDescInfo() {
            return descInfo;
        }

        public void setDescInfo(String descInfo) {
            this.descInfo = descInfo;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "Propa{" +
                    "descInfo='" + descInfo + '\'' +
                    ", id=" + id +
                    ", type=" + type +
                    ", time=" + time +
                    ", logo='" + logo + '\'' +
                    ", name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", imgArray=" + imgArray +
                    ", videoArray=" + videoArray +
                    '}';
        }
    }


}
