package com.ziyiou.netshare.operation.download;

import com.ziyiou.netshare.operation.download.domain.DownloadFile;
import jakarta.servlet.http.HttpServletResponse;

public abstract class Downloader {
    public abstract void download(HttpServletResponse httpServletResponse, DownloadFile uploadFile);
}
