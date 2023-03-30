package com.ziyiou.netshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.vo.UserFileListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {
    List<UserFileListVO> userFileList(UserFile userFile, Long beginCount, Long pageCount);
    List<UserFileListVO> selectFileByExtendName(List<String> filenameList, Long beginCount, Long pageCount, long userId);
    Long selectCountByExtendName(List<String> filenameList, Long beginCount, Long pageCount, long userId);
    List<UserFileListVO> selectFileNotInExtendName(List<String> filenameList, Long beginCount, Long pageCount, long userId);
    Long selectCountNotInExtendName(List<String> filenameList, Long beginCount, Long pageCount, long userId);
}
