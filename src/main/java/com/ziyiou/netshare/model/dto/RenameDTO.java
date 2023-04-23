package com.ziyiou.netshare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "重命名DTO")
public class RenameDTO {
    @Schema(description="新文件名")
    private String newName;
    @Schema(description="用户文件id")
    private long userFileId;
}
