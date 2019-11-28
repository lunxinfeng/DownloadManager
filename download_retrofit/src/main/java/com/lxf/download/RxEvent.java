package com.lxf.download;

class RxEvent {
    private int code;
    private Object object;

    RxEvent(int code, Object object) {
        this.code = code;
        this.object = object;
    }

    public RxEvent() {
    }

    int getCode() {
        return code;
    }

    Object getObject() {
        return object;
    }
}
