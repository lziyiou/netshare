package com.ziyiou.netshare.operation;

import com.ziyiou.netshare.operation.upload.Uploader;

public interface FileOperationFactory {
    Uploader getUploader();
}
