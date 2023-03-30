package com.ziyiou.netshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "上传文件DTO")
public class UploadFileDTO {
    @Schema(description = "文件路径")
    private String filepath;

    @Schema(description = "上传时间")
    private String uploadTime;



    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "文件大小")
    private Long filesize;

    @Schema(description = "切片数量")
    private int chunkNumber;

    @Schema(description = "切片大小")
    private long chunkSize;

    @Schema(description = "所有切片")
    private int totalChunks;
    @Schema(description = "总大小")
    private long totalSize;
    @Schema(description = "当前切片大小")
    private long currentChunkSize;
    @Schema(description = "md5码")
    private String identifier;

}