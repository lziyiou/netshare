package com.ziyiou.netshare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.User;

public interface UserService extends IService<User> {
    RestResult<String> register(User user);
    RestResult<User> login(User user);
    User getUserByToken(String token);
}
