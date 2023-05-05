package com.ziyiou.netshare.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ziyiou.netshare.mapper.FileMapper;
import com.ziyiou.netshare.mapper.UserFileMapper;
import com.ziyiou.netshare.model.File;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.UploadFileDTO;
import com.ziyiou.netshare.operation.FileOperationFactory;
import com.ziyiou.netshare.operation.download.Downloader;
import com.ziyiou.netshare.operation.download.domain.DownloadFile;
import com.ziyiou.netshare.operation.upload.Uploader;
import com.ziyiou.netshare.operation.upload.domain.UploadFile;
import com.ziyiou.netshare.service.FileTransferService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.util.PropertiesUtil;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileTransferServiceImpl implements FileTransferService {
    @Resource
    FileMapper fileMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    FileOperationFactory localStorageOperationFactory;
    @Resource
    UserFileService userFileService;

    @Override
    public UserFile uploadFile(MultipartFile multipartFile, UploadFileDTO uploadFileDTO, Long userId) {
        Uploader uploader = null;
        UploadFile uploadFile = new UploadFile();
        uploadFile.setChunkNumber(uploadFileDTO.getChunkNumber());
        uploadFile.setChunkSize(uploadFileDTO.getChunkSize());
        uploadFile.setTotalSize(uploadFileDTO.getTotalSize());
        uploadFile.setTotalChunks(uploadFileDTO.getTotalChunks());
        uploadFile.setCurrentChunkSize(uploadFileDTO.getCurrentChunkSize());
        uploadFile.setIdentifier(uploadFileDTO.getIdentifier());
        String storageType = PropertiesUtil.getProperty("file.storage-type");
        synchronized (FileTransferService.class) {
            if ("0".equals(storageType)) {
                uploader = localStorageOperationFactory.getUploader();
            }
        }

        // 上传文件
        String fpath = uploader.upload(multipartFile, uploadFile);


        // 上传成功判断
        UserFile userFile = null;
        if (uploadFile.getChunkNumber() == uploadFile.getTotalChunks()) {
            File file = new File();
            file.setFileUrl(fpath);
            file.setIdentifier(uploadFileDTO.getIdentifier());
            file.setFilesize(uploadFileDTO.getTotalSize());
            file.setPointCount(1);
            file.setStorageType(0);
            // 插入到file表中
            fileMapper.insert(file);
            //查出新插入数据的fileId

            // 插入到userFile表中，
            userFile = new UserFile();
            userFile.setFilepath(uploadFileDTO.getFilepath());
            userFile.setUploadTime(DateTime.now());
            userFile.setFilename(multipartFile.getOriginalFilename());
            userFile.setFileId(file.getFileId());
            userFile.setUserId(userId);
            userFile.setIsDir(0);
            userFile.setExtendName(FileUtil.extName(multipartFile.getOriginalFilename()));
            long parentId = 0;
            if (!uploadFileDTO.getFilepath().equals("/")) {
                QueryWrapper<UserFile> queryWrapper = new QueryWrapper<>();
                String filepath = uploadFileDTO.getFilepath().substring(0, uploadFileDTO.getFilepath().lastIndexOf('/'));
                String parentPath = filepath.substring(0, filepath.lastIndexOf('/') + 1);
                String parentName = filepath.substring(filepath.lastIndexOf('/') + 1);
                queryWrapper.eq("filepath", parentPath).eq("filename", parentName);
                UserFile parentUserfile = userFileService.getOne(queryWrapper);
                parentId = parentUserfile.getUserFileId();
            }
            userFile.setParentId(parentId);
            userFileMapper.insert(userFile);

        }

        return userFile;
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(Long userFileId) {
        UserFile userFile = userFileMapper.selectById(userFileId);

        String fileName = userFile.getFilename() + "." + userFile.getExtendName();

        File file = fileMapper.selectById(userFile.getFileId());
        Downloader downloader = null;
        if (file.getStorageType() == 0) {
            downloader = localStorageOperationFactory.getDownloader();
        }
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setFileUrl(file.getFileUrl());
        downloadFile.setTimeStampName(file.getTimeStampName());
        downloadFile.setFilename(fileName);

        return downloader.download(downloadFile);
    }
}
