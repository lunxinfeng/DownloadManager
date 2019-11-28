package com.lxf.download;

/**
 * 文件下载进度管理类
 * Created by lxf on 2017/3/3.
 */
class FileLoadingBean {
    private long total;
    private long bytesReaded;

    FileLoadingBean(long total, long bytesReaded) {
        this.total = total;
        this.bytesReaded = bytesReaded;
    }

    long getTotal() {
        return total;
    }

    void setTotal(long total) {
        this.total = total;
    }

    long getBytesReaded() {
        return bytesReaded;
    }

    void setBytesReaded(long bytesReaded) {
        this.bytesReaded = bytesReaded;
    }
}
