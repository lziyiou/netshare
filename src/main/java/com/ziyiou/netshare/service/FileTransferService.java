package com.ziyiou.netshare.service;

import com.ziyiou.netshare.model.dto.UploadFileDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileTransferService {
    void uploadFile(MultipartFile multipartFile, UploadFileDTO uploadFileDto, Long userId);
    void downloadFile(HttpServletResponse httpServletResponse, Long userFileId);
}
