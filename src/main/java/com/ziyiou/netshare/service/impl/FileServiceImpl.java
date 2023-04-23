package com.ziyiou.netshare.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziyiou.netshare.mapper.FileMapper;
import com.ziyiou.netshare.model.File;
import com.ziyiou.netshare.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    @Resource
    private FileMapper fileMapper;

    @Override
    public void increaseFilePointCount(Long fileId) {
        fileMapper.increaseFilePointCount(fileId);
    }

    @Override
    public void decreaseFilePointCount(Long fileId) {
        fileMapper.decreaseFilePointCount(fileId);
        // 检查索引数量是不是已经为0
        File file = fileMapper.selectById(fileId);
        if (file.getPointCount()==0) {
            // 删除数据库记录
            fileMapper.deleteById(fileId);
            // todo 删除服务器文件
            FileUtil.del(file.getFileUrl());
        }
    }
}
