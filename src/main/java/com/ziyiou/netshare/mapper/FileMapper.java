package com.ziyiou.netshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziyiou.netshare.model.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    void increaseFilePointCount(Long fileId);

    void decreaseFilePointCount(Long fileId);
}
