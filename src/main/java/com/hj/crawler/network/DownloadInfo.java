package com.hj.crawler.network;

import lombok.Data;
import lombok.NonNull;

@Data
public class DownloadInfo {

    @NonNull
    private String url;
    @NonNull
    private String destDir;
    @NonNull
    private String destName;
}
