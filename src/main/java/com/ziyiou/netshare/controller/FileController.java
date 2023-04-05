package com.ziyiou.netshare.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.dto.CreateFileDTO;
import com.ziyiou.netshare.dto.UserFileListDTO;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.service.FileService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.service.UserService;
import com.ziyiou.netshare.vo.UserFileListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "file", description = "该接口为文件接口，主要用来做一些文件的基本操作，如创建目录，删除，移动，复制等。")
@RestController
@Slf4j
public class FileController {
    @Resource
    FileService fileService;
    @Resource
    UserService userService;
    @Resource
    UserFileService userFileService;

    /**
     * 创建文件
     * @param createFileDTO
     * @param token
     * @return
     */
    @Operation(summary = "创建目录", description = "目录(文件夹)的创建", tags = {"file"})
    @PostMapping(value = "/dir")
    public RestResult<String> createFile(@RequestBody CreateFileDTO createFileDTO,
                                         @RequestHeader("token")String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        // 检查当前用户下是否有同目录下的文件名
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFilename, createFileDTO.getFilename())
                .eq(UserFile::getFilepath, createFileDTO.getFilepath())
                .eq(UserFile::getUserId, userByToken.getUserId());
        List<UserFile> userFileList = userFileService.list(lambdaQueryWrapper);
        if (!userFileList.isEmpty()) {
            return RestResult.fail().message("同目录下文件名重复");
        }

        UserFile userFile = new UserFile();
        userFile.setUserId(userByToken.getUserId());
        userFile.setFilename(createFileDTO.getFilename());
        userFile.setFilepath(createFileDTO.getFilepath());
        userFile.setIsDir(1);
        userFile.setUploadTime(DateUtil.date());

        userFileService.save(userFile);
        return RestResult.success();
    }

    /**
     * 获取文件列表
     * @param userFileListDTO
     * @param token
     * @return
     */
    @Operation(summary = "获取文件列表", description = "用来做前台文件列表展示", tags = {"file"})
    @GetMapping(value = "/fileList")
    public RestResult<UserFileListVO> getUserFileList(UserFileListDTO userFileListDTO,
                                                      @RequestHeader("token") String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        // 查询数据
        List<UserFileListVO> fileList = userFileService.getUserFileByFilePath(userFileListDTO.getFilepath(),
                userByToken.getUserId(), userFileListDTO.getCurrentPage(), userFileListDTO.getPageCount());

        // 查询数据总数
        LambdaQueryWrapper<UserFile> userFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFileLambdaQueryWrapper.eq(UserFile::getUserId, userByToken.getUserId())
                .eq(UserFile::getFilepath, userFileListDTO.getFilepath());
        long total = userFileService.count(userFileLambdaQueryWrapper);

        // 返回数据
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("list", fileList);

        return RestResult.success().data(map);
    }

    /**
     * 通过文件类型选择文件
     * @param filetype
     * @param currentPage
     * @param pageCount
     * @param token
     * @return
     */
    @Operation(summary = "通过文件类型选择文件", description = "该接口可以实现文件格式分类查看", tags = {"file"})
    @GetMapping(value = "/fileList/type")
    public RestResult<List<Map<String, Object>>> selectFileByFiletype(int filetype,
                                                                      Long currentPage,
                                                                      Long pageCount,
                                                                      @RequestHeader("token")String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        Map<String, Object> map = userFileService.getUserFileByType(filetype, currentPage,
                pageCount, userByToken.getUserId());
        return RestResult.success().data(map);
    }


}
