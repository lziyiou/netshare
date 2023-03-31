package com.ziyiou.netshare.operation;

import com.ziyiou.netshare.operation.delete.Deleter;
import com.ziyiou.netshare.operation.download.Downloader;
import com.ziyiou.netshare.operation.upload.Uploader;

public interface FileOperationFactory {
    Uploader getUploader();
    Downloader getDownloader();
    Deleter getDeleter();
}
