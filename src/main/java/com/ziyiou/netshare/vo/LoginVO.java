package com.ziyiou.netshare.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *  登录后响应
 *         用户名、token
 */
@Data
@Schema(description="登录VO")
public class LoginVO {
    @Schema(description="用户名")
    private String username;

    @Schema(description="token")
    private String token;
}