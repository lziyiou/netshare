package com.ziyiou.netshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.vo.UserFileListVO;

import java.util.List;
import java.util.Map;

public interface UserFileService extends IService<UserFile> {
    List<UserFileListVO> getUserFileByFilePath(String filepath, Long userId, Long currentPage, Long pageCount);
    Map<String, Object> getUserFileByType(int fileType, Long currentPage, Long pageCount, Long userId);
}
