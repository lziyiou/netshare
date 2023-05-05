package com.ziyiou.netshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziyiou.netshare.model.Share;
import com.ziyiou.netshare.model.vo.ShareVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShareMapper extends BaseMapper<Share> {
    List<ShareVo> getShareInfo(long userId);
}
