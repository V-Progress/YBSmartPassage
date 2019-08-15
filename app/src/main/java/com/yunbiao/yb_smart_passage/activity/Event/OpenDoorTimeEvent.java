package com.yunbiao.yb_smart_passage.activity.Event;

public class OpenDoorTimeEvent {
    int time = 0;

    public OpenDoorTimeEvent(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
