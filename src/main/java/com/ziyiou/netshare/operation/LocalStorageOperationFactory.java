package com.ziyiou.netshare.operation;

import com.ziyiou.netshare.operation.delete.Deleter;
import com.ziyiou.netshare.operation.delete.product.LocalStorageDeleter;
import com.ziyiou.netshare.operation.download.Downloader;
import com.ziyiou.netshare.operation.download.product.LocalStorageDownloader;
import com.ziyiou.netshare.operation.upload.Uploader;
import com.ziyiou.netshare.operation.upload.product.LocalStorageUploader;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageOperationFactory implements FileOperationFactory {
    @Resource
    LocalStorageUploader localStorageUploader;
    @Resource
    LocalStorageDownloader localStorageDownloader;
    @Resource
    LocalStorageDeleter localStorageDeleter;
    @Override
    public Uploader getUploader() {
        return localStorageUploader;
    }

    @Override
    public Downloader getDownloader() {
        return localStorageDownloader;
    }

    @Override
    public Deleter getDeleter() {
        return localStorageDeleter;
    }
}
