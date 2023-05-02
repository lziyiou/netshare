package com.ziyiou.netshare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "移动文件DTO",required = true)
public class MoveFileDTO {

    @Schema(description = "文件id")
    private Long userFileId;

    @Schema(description = "父目录id")
    private Long parentId;

}