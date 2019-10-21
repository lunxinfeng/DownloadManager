package com.lxf.download;

public interface DownloadListener {
    void onStart(long totalLength);
    void onProgress(int progress);
    void onComplete();
    void onFail();
}
