package com.ziyiou.netshare.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "分享文件VO")
public class ShareVo {
    @Schema(description = "文件名")
    private String filename;
    @Schema(description = "链接")
    private String link;
    @Schema(description = "有效期")
    private int exp;
    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "状态（过期时间）")
    private String status;
}

