package com.ziyiou.netshare.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.CreateFileDTO;
import com.ziyiou.netshare.model.dto.UserFileListDTO;
import com.ziyiou.netshare.model.vo.UserFileListVO;
import com.ziyiou.netshare.service.FileService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.service.UserService;
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
     * 创建目录
     *
     * @param createFileDTO 文件名、文件路径
     * @param token         token
     * @return 是否成功
     */
    @Operation(summary = "创建目录", description = "目录(文件夹)的创建", tags = {"file"})
    @PostMapping(value = "/dir")
    public RestResult<String> createDir(@RequestBody CreateFileDTO createFileDTO,
                                        @RequestHeader("token") String token) {
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
     *
     * @param userFileListDTO 文件路径、页码、一页的数量
     * @param token           token
     * @return 文件列表
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

        // 查询数据总数量
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
     *
     * @param filetype    文件类型
     * @param currentPage 页码
     * @param pageCount   一页的数量
     * @param token       token
     * @return 文件列表
     */
    @Operation(summary = "通过文件类型选择文件", description = "该接口可以实现文件格式分类查看", tags = {"file"})
    @GetMapping(value = "/fileList/type")
    public RestResult<List<Map<String, Object>>> selectFileByFiletype(int filetype,
                                                                      Long currentPage,
                                                                      Long pageCount,
                                                                      @RequestHeader("token") String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        Map<String, Object> map = userFileService.getUserFileByType(filetype, currentPage,
                pageCount, userByToken.getUserId());
        return RestResult.success().data(map);
    }


    @DeleteMapping("/file")
    public RestResult<List<Map<String, Object>>> deleteFile(int userFileId, @RequestHeader("token") String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        UserFile userFile = userFileService.getById(userFileId);

        if (userFile.getIsDir() == 1) {
            // 查出子级文件并删除
            LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.likeRight(UserFile::getFilepath,
                    userFile.getFilepath() + userFile.getFilename());
            List<UserFile> userFileList = userFileService.list(lambdaQueryWrapper);

            for (UserFile uf : userFileList) {
                userFileService.removeById(uf.getUserFileId());
                if (uf.getIsDir() == 0) {
                    // 不是目录，索引数量-1
                    fileService.decreaseFilePointCount(uf.getFileId());
                }
            }

            // 删除选中项
            userFileService.removeById(userFileId);
        } else {
            // 当前文件不是目录，索引数量-1
            fileService.decreaseFilePointCount(userFile.getFileId());

            // 删除选中项
            userFileService.removeById(userFileId);
        }

        return null;
    }

}

