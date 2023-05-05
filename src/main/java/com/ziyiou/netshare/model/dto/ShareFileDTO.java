package com.ziyiou.netshare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "分享文件DTO")
public class ShareFileDTO {
    @Schema(description="分享的文件id")
    private long userFileId;
    @Schema(description="分享形式： 0-私密链接， 1-公开链接")
    private int form;
    @Schema(description="过期时间")
    private int exp;
}