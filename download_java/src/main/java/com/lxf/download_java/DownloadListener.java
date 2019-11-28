package com.lxf.download_java;

public interface DownloadListener {
    void onStart(long totalLength);
    void onProgress(int progress);
    void onComplete();
    void onFail();
}
