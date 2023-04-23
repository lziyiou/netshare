package com.ziyiou.netshare.operation.delete.product;

import com.ziyiou.netshare.operation.delete.Deleter;
import com.ziyiou.netshare.operation.delete.domain.DeleteFile;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageDeleter extends Deleter {
    @Override
    public void delete(DeleteFile deleteFile) {
//        File file = new File(PathUtil.getStaticPath() + deleteFile.getFileUrl());
//        if (file.exists()) {
//            file.delete();
//        }
//
//        if (FileUtil.isImageFile(deleteFile.getFileUrl())) {
//            File minFile = new File(PathUtil.getStaticPath() + deleteFile.getFileUrl().replace(deleteFile.getTimeStampName(), deleteFile.getTimeStampName() + "_min"));
//            if (minFile.exists()) {
//                minFile.delete();
//            }
//        }
    }
}