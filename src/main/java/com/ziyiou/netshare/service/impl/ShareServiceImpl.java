package com.ziyiou.netshare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziyiou.netshare.mapper.ShareMapper;
import com.ziyiou.netshare.model.Share;
import com.ziyiou.netshare.model.vo.ShareVo;
import com.ziyiou.netshare.service.ShareService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    @Resource
    private ShareMapper shareMapper;

    @Override
    public List<ShareVo> getShareInfo(long userId) {
        return shareMapper.getShareInfo(userId);
    }
}
