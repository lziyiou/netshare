package com.ziyiou.netshare.operation;

import com.ziyiou.netshare.operation.upload.Uploader;
import com.ziyiou.netshare.operation.upload.product.LocalStorageUploader;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageOperationFactory implements FileOperationFactory {
    @Resource
    LocalStorageUploader localStorageUploader;

    @Override
    public Uploader getUploader() {
        return localStorageUploader;
    }
}
