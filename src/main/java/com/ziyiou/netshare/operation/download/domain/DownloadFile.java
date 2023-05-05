package com.ziyiou.netshare.operation.download.domain;

import lombok.Data;

@Data
public class DownloadFile {
    private String fileUrl;
    private String timeStampName;
    private String filename;
}