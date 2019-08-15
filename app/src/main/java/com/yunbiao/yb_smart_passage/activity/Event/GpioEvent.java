package com.yunbiao.yb_smart_passage.activity.Event;

public class GpioEvent {
    boolean is;
    public static final int OPEN = 1;
    int openState = 0;

    public GpioEvent(int openState) {
        this.openState = openState;
    }

    public GpioEvent(boolean is) {
        this.is = is;
    }

    public boolean isIs() {
        return is;
    }
    public int getState() {
        return openState;
    }
}
