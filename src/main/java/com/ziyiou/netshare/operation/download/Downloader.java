package com.ziyiou.netshare.operation.download;

import com.ziyiou.netshare.operation.download.domain.DownloadFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public abstract class Downloader {
    public abstract ResponseEntity<Resource> download(DownloadFile uploadFile);
}
