package com.ziyiou.netshare.operation.download.product;

import com.ziyiou.netshare.operation.download.Downloader;
import com.ziyiou.netshare.operation.download.domain.DownloadFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LocalStorageDownloader extends Downloader {
    @Override
    public ResponseEntity<Resource> download(DownloadFile downloadFile) {
        // 从文件系统中加载文件
        Resource resource = new FileSystemResource(downloadFile.getFileUrl());

        // 设置 Content-Disposition 响应头部信息，用于告诉浏览器下载文件
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + downloadFile.getFilename());

        // 创建 ResponseEntity 对象，包装资源和响应头部信息
        try {
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
