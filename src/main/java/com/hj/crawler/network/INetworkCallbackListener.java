package com.hj.crawler.network;

public interface INetworkCallbackListener {

    void onProgress(int progress);

    void onSuccess(String destPath);

    void onFailure(Exception ex);
}
