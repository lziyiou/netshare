package com.ziyiou.netshare.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "上传文件DTO")
public class UploadFileDTO {
    @Schema(description = "文件路径")
    private String filepath;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "切片序号")
    private int chunkNumber;
    @Schema(description = "切片数量")
    private int totalChunks;
    @Schema(description = "切片大小")
    private long chunkSize;
    @Schema(description = "当前切片大小")
    private long currentChunkSize;
    @Schema(description = "总大小")
    private long totalSize;
    @Schema(description = "md5码")
    private String identifier;

}