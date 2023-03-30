package com.ziyiou.netshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *  注册用户时提供：
 *      用户名、手机号、密码
 */
@Data
@Schema(description="注册DTO")
public class RegisterDTO {
    @Schema(description="用户名")
    private String username;

    @Schema(description="手机号")
    private String telephone;

    @Schema(description="密码")
    private String password;
}