package com.ziyiou.netshare.service.impl;

import cn.hutool.core.date.DateUtil;
import com.ziyiou.netshare.dto.UploadFileDTO;
import com.ziyiou.netshare.mapper.FileMapper;
import com.ziyiou.netshare.mapper.UserFileMapper;
import com.ziyiou.netshare.model.File;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.operation.FileOperationFactory;
import com.ziyiou.netshare.operation.upload.Uploader;
import com.ziyiou.netshare.operation.upload.domain.UploadFile;
import com.ziyiou.netshare.service.FileTransferService;
import com.ziyiou.netshare.util.PropertiesUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileTransferServiceImpl implements FileTransferService {
    @Resource
    FileMapper fileMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    FileOperationFactory localStorageOperationFactory;
    @Override
    public void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, Long userId) {
        Uploader uploader = null;
        UploadFile uploadFile = new UploadFile();
        uploadFile.setChunkNumber(uploadFileDTO.getChunkNumber());
        uploadFile.setChunkSize(uploadFileDTO.getChunkSize());
        uploadFile.setTotalChunks(uploadFileDTO.getTotalChunks());
        uploadFile.setIdentifier(uploadFileDTO.getIdentifier());
        uploadFile.setTotalSize(uploadFileDTO.getTotalSize());
        uploadFile.setCurrentChunkSize(uploadFileDTO.getCurrentChunkSize());
        String storageType = PropertiesUtil.getProperty("file.storage-type");
        synchronized (FileTransferService.class) {
            if ("0".equals(storageType)) {
                uploader = localStorageOperationFactory.getUploader();
            }
        }

        List<UploadFile> uploadFileList = uploader.upload(request, uploadFile);
        for (int i = 0; i < uploadFileList.size(); i++){
            uploadFile = uploadFileList.get(i);
            File file = new File();

            file.setIdentifier(uploadFileDTO.getIdentifier());
            file.setStorageType(Integer.parseInt(storageType));
            file.setTimeStampName(uploadFile.getTimeStampName());
            if (uploadFile.getSuccess() == 1){
                file.setFileUrl(uploadFile.getUrl());
                file.setFileSize(uploadFile.getFilesize());
                file.setPointCount(1);
                fileMapper.insert(file);
                UserFile userFile = new UserFile();
                userFile.setFileId(file.getFileId());
                userFile.setExtendName(uploadFile.getFiletype());
                userFile.setFilename(uploadFile.getFilename());
                userFile.setFilepath(uploadFileDTO.getFilepath());
                //userFile.setDeleteFlag(0);
                userFile.setUserId(userId);
                userFile.setIsDir(0);
                userFile.setUploadTime(DateUtil.date());
                userFileMapper.insert(userFile);
            }

        }
    }

    @Override
    public void downloadFile(HttpServletResponse httpServletResponse, Long userFileId) {

    }
}
