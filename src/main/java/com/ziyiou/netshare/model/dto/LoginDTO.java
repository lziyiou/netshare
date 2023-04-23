package com.ziyiou.netshare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *  登录用户时提供：
 *      手机号、密码
 */
@Data
@Schema(description="登录DTO")
public class LoginDTO {
    @Schema(description="手机号")
    private String telephone;

    @Schema(description="密码")
    private String password;
}