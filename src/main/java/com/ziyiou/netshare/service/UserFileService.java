package com.ziyiou.netshare.service;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.MoveFileDTO;
import com.ziyiou.netshare.model.dto.ShareFileDTO;
import com.ziyiou.netshare.model.vo.UserFileListVO;

import java.util.List;
import java.util.Map;

public interface UserFileService extends IService<UserFile> {
    List<UserFileListVO> getUserFileByFilepath(String filepath, Long userId, Long currentPage, Long pageCount);
    Map<String, Object> getUserFileByType(int fileType, Long currentPage, Long pageCount, Long userId);

    RestResult rename(Long userFileId, String newName);

    JSONArray getDirTree(Long userId);

    void moveFile(MoveFileDTO moveFileDTO);

    String generateShareLink(ShareFileDTO shareFileDTO);
}
