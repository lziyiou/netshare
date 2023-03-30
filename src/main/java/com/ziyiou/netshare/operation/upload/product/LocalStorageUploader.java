package com.ziyiou.netshare.operation.upload.product;

import com.ziyiou.netshare.operation.upload.Uploader;
import com.ziyiou.netshare.operation.upload.domain.UploadFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalStorageUploader extends Uploader {

    public LocalStorageUploader() {

    }

    @Override
    public List<UploadFile> upload(HttpServletRequest request, UploadFile uploadFile) {
        return null;
    }
}
