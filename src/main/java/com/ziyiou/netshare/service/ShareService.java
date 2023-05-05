package com.ziyiou.netshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziyiou.netshare.model.Share;
import com.ziyiou.netshare.model.vo.ShareVo;

import java.util.List;

public interface ShareService extends IService<Share> {
    List<ShareVo> getShareInfo(long userId);
}
