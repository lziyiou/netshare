package com.ziyiou.netshare.operation.upload;

import cn.hutool.core.io.FileUtil;
import com.ziyiou.netshare.operation.upload.domain.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Slf4j
public abstract class Uploader {
    public abstract String upload(MultipartFile multipartFile, UploadFile uploadFile);

    public synchronized boolean checkUploadStatus(UploadFile param, File confFile) throws IOException {
        RandomAccessFile confAccessFile = new RandomAccessFile(confFile, "rw");
        //设置文件长度
        confAccessFile.setLength(param.getTotalChunks());
        //设置起始偏移量
        confAccessFile.seek(param.getChunkNumber() - 1);
        //将指定的一个字节写入文件中 127，
        confAccessFile.write(Byte.MAX_VALUE);
//        byte[] completeStatusList = FileUtils.readFileToByteArray(confFile);
        byte[] completeStatusList = FileUtil.readBytes(confFile);
        confAccessFile.close();//不关闭会造成无法占用
        //创建conf文件文件长度为总分片数，每上传一个分块即向conf文件中写入一个127，那么没上传的位置就是默认的0,已上传的就是127
        for (int i = 0; i < completeStatusList.length; i++) {
            if (completeStatusList[i] != Byte.MAX_VALUE) {
                return false;
            }
        }
        FileUtil.del(confFile);
        return true;
    }

}
