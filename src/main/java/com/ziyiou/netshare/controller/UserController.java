package com.ziyiou.netshare.controller;

import cn.hutool.core.date.DateBetween;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.dto.LoginDTO;
import com.ziyiou.netshare.model.dto.RegisterDTO;
import com.ziyiou.netshare.model.vo.LoginVO;
import com.ziyiou.netshare.model.vo.ShareVo;
import com.ziyiou.netshare.service.ShareService;
import com.ziyiou.netshare.service.UserService;
import com.ziyiou.netshare.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "user", description = "该接口为用户接口，主要做用户登录，注册和校验token")
public class UserController {
    @Resource
    UserService userService;
    @Resource
    private ShareService shareService;

    /**
     * 注册接口
     *
     * @param registerDTO 用户名 手机号 密码
     * @return 登录成功或失败
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册账号", tags = {"user"})
    public RestResult<String> register(@RequestBody RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setTelephone(registerDTO.getTelephone());
        user.setPassword(registerDTO.getPassword());

        return userService.register(user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录认证后才能进入系统", tags = {"user"})
    public RestResult<LoginVO> userLogin(@RequestBody LoginDTO loginDTO) {
        User user = new User();
        user.setTelephone(loginDTO.getTelephone());
        user.setPassword(loginDTO.getPassword());

        // 登录
        RestResult<User> result = userService.login(user);
        // 判断登录结果
        if (!result.getSuccess()) {
            return RestResult.fail().message(result.getMessage());
        }

        // 返回登录信息： 用户名、token
        String jwt = JwtUtil.createJWT(result.getData());
        LoginVO loginVO = new LoginVO();
        loginVO.setUsername(result.getData().getUsername());
        loginVO.setToken(jwt);
        return RestResult.success().data(loginVO);
    }

    @GetMapping("/checkUserLogin")
    @Operation(summary = "检查用户登录信息", description = "验证token的有效性", tags = {"user"})
    public RestResult<User> checkToken(@RequestHeader("token") String token) {
        User userByToken = userService.getUserByToken(token);

        if (userByToken != null) {
            return RestResult.success().data(userByToken);
        } else {
            return RestResult.fail().message("用户暂未登录！");
        }
    }

    @GetMapping("/user")
    @Operation(summary = "获取用户信息", description = "获取用户信息", tags = {"user"})
    public RestResult<List<ShareVo>> getUserInfo(@RequestHeader("token") String token) {
        User userByToken = userService.getUserByToken(token);

        if (userByToken == null) {
            return RestResult.fail().message("用户暂未登录！");
        }

        // 获取用户的分享数据记录

        List<ShareVo> list = shareService.getShareInfo(userByToken.getUserId());

        list.forEach(item -> {
            String status;
            if (item.getExp() == -1) {
                status = "永久有效";
            } else {
                DateTime expDay = DateUtil.offsetDay(item.getCreateTime(), item.getExp());
                DateBetween between = expDay.between(DateTime.now());
                status = between.between(DateUnit.DAY) + "天" + between.between(DateUnit.HOUR) % 24 + "小时";
            }
            item.setStatus(status);
        });

        return RestResult.success().data(list);
    }
}
