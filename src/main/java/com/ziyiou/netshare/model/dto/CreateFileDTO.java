package com.ziyiou.netshare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "创建文件DTO")
public class CreateFileDTO {
    @Schema(description="文件名")
    private String filename;
    @Schema(description="文件路径")
    private String filepath;
}
