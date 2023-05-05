package com.ziyiou.netshare.service;

import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.UploadFileDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileTransferService {
    UserFile uploadFile(MultipartFile multipartFile, UploadFileDTO uploadFileDto, Long userId);
    ResponseEntity<Resource> downloadFile(Long userFileId);
}
