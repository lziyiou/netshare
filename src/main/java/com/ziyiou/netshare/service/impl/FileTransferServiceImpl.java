package com.ziyiou.netshare.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;

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
            userFileMapper.insert(userFile);


            // 如果是视频文件 直接生成分片文件mpd信息
            // 生成视频流文件
            // 获取视频路径
            String filepath = file.getFileUrl();

            // 将视频分片文件暂存到服务器目录下，定期清理
            String parent = FileUtil.getParent(filepath, 1);
            // 暂存到同级目录下，新建同名文件夹。
            String slicePath = parent + java.io.File.separator + file.getIdentifier() + java.io.File.separator;

        }

        return userFile;
    }

    @Override
    public void downloadFile(HttpServletResponse httpServletResponse, Long userFileId) {
        UserFile userFile = userFileMapper.selectById(userFileId);

        String fileName = userFile.getFilename() + "." + userFile.getExtendName();
        try {
            fileName = new String(fileName.getBytes("utf-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpServletResponse.setContentType("application/force-download");// 设置强制下载不打开
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名


        File file = fileMapper.selectById(userFile.getFileId());
        Downloader downloader = null;
        if (file.getStorageType() == 0) {
            downloader = localStorageOperationFactory.getDownloader();
        }
        DownloadFile uploadFile = new DownloadFile();
        uploadFile.setFileUrl(file.getFileUrl());
        uploadFile.setTimeStampName(file.getTimeStampName());
        downloader.download(httpServletResponse, uploadFile);
    }
}
