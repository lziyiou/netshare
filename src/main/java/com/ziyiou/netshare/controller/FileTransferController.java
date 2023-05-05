package com.ziyiou.netshare.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.File;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.UploadFileDTO;
import com.ziyiou.netshare.model.vo.UploadFileVO;
import com.ziyiou.netshare.service.FileService;
import com.ziyiou.netshare.service.FileTransferService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "fileTransfer", description = "该接口为文件传输接口，主要用来做文件的上传和下载")
@RestController
public class FileTransferController {
    @Resource
    UserService userService;
    @Resource
    FileService fileService;
    @Resource
    UserFileService userFileService;
    @Resource
    FileTransferService fileTransferService;

    @Operation(summary = "极速上传", description = "校验文件MD5判断文件是否存在，如果存在直接上传成功并返回skipUpload=true，" +
            "如果不存在返回skipUpload=false需要再次调用该接口的POST方法", tags = {"fileTransfer"})
    @GetMapping("/upload")
    public RestResult<UploadFileVO> uploadFileSpeed(UploadFileDTO uploadFileDTO,
                                                    @RequestHeader("token") String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        UploadFileVO uploadFileVO = new UploadFileVO();

        Map<String, Object> param = new HashMap<>();
        param.put("identifier", uploadFileDTO.getIdentifier());
        List<File> fileList = fileService.listByMap(param);
        if (fileList != null && !fileList.isEmpty()) {
            File file = fileList.get(0);

            // 当前用户已拥有该文件
            LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserFile::getFileId, file.getFileId());
            long count = userFileService.count(lambdaQueryWrapper);
            if (count == 0) {
                UserFile userfile = new UserFile();
                userfile.setFileId(file.getFileId());
                userfile.setUserId(userByToken.getUserId());
                userfile.setFilepath(uploadFileDTO.getFilepath());
                userfile.setFilename(FileUtil.getName(uploadFileDTO.getFilename()));
                userfile.setExtendName(FileUtil.extName(uploadFileDTO.getFilename()));
                userfile.setIsDir(0);
                userfile.setUploadTime(DateUtil.date());
                userFileService.save(userfile);
                fileService.increaseFilePointCount(file.getFileId());
            }
            uploadFileVO.setSkipUpload(true);
        } else {
            uploadFileVO.setSkipUpload(false);
        }
        return RestResult.success().data(uploadFileVO);
    }

    @Operation(summary = "上传文件", description = "真正的上传文件接口", tags = {"fileTransfer"})
    @PostMapping(value = "/upload")
    public RestResult<UploadFileVO> uploadFile(MultipartFile multipartFile,
                                               UploadFileDTO uploadFileDto,
                                               @RequestHeader("token") String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        UserFile userFile = fileTransferService.uploadFile(multipartFile, uploadFileDto, userByToken.getUserId());
        UploadFileVO uploadFileVo = new UploadFileVO();
        uploadFileVo.setUserFile(userFile);
        return RestResult.success().data(uploadFileVo);

    }

    @Operation(summary = "下载文件", description = "下载文件接口", tags = {"fileTransfer"})
    @GetMapping(value = "/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(Long userFileId,
                                                                             @RequestHeader("token") String token,
                                                                             boolean isShare) {
        // 验证用户认证状态
        if (!isShare) {
            User userByToken = userService.getUserByToken(token);
            if (userByToken == null) {
                return null;
            }
        }

        return fileTransferService.downloadFile(userFileId);
    }
}
