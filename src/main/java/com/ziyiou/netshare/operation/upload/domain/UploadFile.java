package com.ziyiou.netshare.operation.upload.domain;

import lombok.Data;

@Data
public class UploadFile {
    private String filename;
    private String filetype;
    private long filesize;
    private String timeStampName;
    private int success;
    private String message;
    private String url;
    //切片上传相关参数
    private String taskId;
    private int chunkNumber;
    private long chunkSize;
    private int totalChunks;
    private String identifier;
    private long totalSize;
    private long currentChunkSize;

}