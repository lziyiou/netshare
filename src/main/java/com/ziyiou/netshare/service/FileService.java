package com.ziyiou.netshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziyiou.netshare.model.File;

public interface FileService extends IService<File> {
    void increaseFilePointCount(Long fileId);
    void decreaseFilePointCount(Long fileId);
}
