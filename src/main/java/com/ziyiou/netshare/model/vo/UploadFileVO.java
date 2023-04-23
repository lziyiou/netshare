package com.ziyiou.netshare.model.vo;

import com.ziyiou.netshare.model.UserFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "文件上传VO",required = true)
public class UploadFileVO {

    @Schema(description = "时间戳", example = "123123123123")
    private String timeStampName;
    @Schema(description = "跳过上传", example = "true")
    private boolean skipUpload;
    @Schema(description = "是否需要合并分片", example = "true")
    private boolean needMerge;
    @Schema(description = "已经上传的分片", example = "[1,2,3]")
    private List<Integer> uploaded;
    @Schema(description = "文件信息")
    private UserFile userFile;

}