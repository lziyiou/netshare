package com.ziyiou.netshare.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.constant.JwtConstant;
import com.ziyiou.netshare.mapper.UserMapper;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public RestResult<String> register(User user) {
        String telephone = user.getTelephone();
        String password = user.getPassword();

        //判断注册信息
        if (!StringUtils.hasLength(telephone) || !StringUtils.hasLength(password)){
            return RestResult.fail().message("手机号或密码不能为空");
        }
        if (isTelephoneExit(telephone)){
            return RestResult.fail().message("手机号已存在！");
        }

        // 使用uuid拼接进行密码加盐
        String salt = UUID.randomUUID().toString().replace("-", "").substring(15);
        String passwordAndSalt = password + salt;
        String newPassword = SecureUtil.md5(passwordAndSalt);

        user.setSalt(salt);
        user.setPassword(newPassword);
        user.setRegisterTime(DateUtil.date());
        int result = userMapper.insert(user);

        if (result == 1) {
            return RestResult.success();
        } else {
            return RestResult.fail().message("注册用户失败，请检查输入信息！");
        }
    }

    @Override
    public RestResult<User> login(User user) {
        String telephone = user.getTelephone();
        String password = user.getPassword();

        // 通过手机号查询用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getTelephone, telephone);
        User saveUser = userMapper.selectOne(lambdaQueryWrapper);
        // 用户输入密码加盐对比
        String salt = saveUser.getSalt();
        String passwordAndSalt = password + salt;
        String newPassword = SecureUtil.md5(passwordAndSalt);
        if (newPassword.equals(saveUser.getPassword())) {
            // 清空密码信息
            saveUser.setPassword("");
            saveUser.setSalt("");
            return RestResult.success().data(saveUser);
        } else {
            return RestResult.fail().message("手机号或密码错误！");
        }
    }

    private boolean isTelephoneExit(String telephone) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getTelephone, telephone);
        List<User> list = userMapper.selectList(lambdaQueryWrapper);
        return list != null && !list.isEmpty();
    }

    @Override
    public User getUserByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        boolean verify = JWTUtil.verify(token, JwtConstant.secret.getBytes());
        if (!verify) {
            return null;
        }

        JWT jwt = JWTUtil.parseToken(token);
        JSONObject sub = (JSONObject) jwt.getPayload("sub");

        return JSONUtil.toBean(sub, User.class);
    }
}
