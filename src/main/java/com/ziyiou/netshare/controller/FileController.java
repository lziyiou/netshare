package com.ziyiou.netshare.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.CreateFileDTO;
import com.ziyiou.netshare.model.dto.RenameDTO;
import com.ziyiou.netshare.model.dto.UserFileListDTO;
import com.ziyiou.netshare.model.vo.UserFileListVO;
import com.ziyiou.netshare.service.FileService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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
        return RestResult.success().data(userFile);
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


    /**
     * 删除文件或目录，如果是目录则删除目录下所有文件
     *
     * @param userFileId 用户文件的id
     * @param token      用户认证信息
     * @return
     */
    @Operation(summary = "删除文件", description = "删除文件", tags = {"file"})
    @DeleteMapping("/file")
    public RestResult deleteFile(int userFileId, @RequestHeader("token") String token) {
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

        return RestResult.success().message("删除成功");
    }

    @Operation(summary = "重命名", description = "重命名", tags = {"file"})
    @PutMapping("/file")
    public RestResult rename(@RequestBody RenameDTO renameDTO, @RequestHeader("token") String token) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        return userFileService.rename(renameDTO.getUserFileId(), renameDTO.getNewName());
    }

    @Operation(summary = "获取图片", description = "获取图片", tags = {"file"})
    @GetMapping("/img")
    public void getImg(long userFileId,
                       @RequestHeader(name = "token", required = false) String tokenByHeader,
                       String tokenByQuery,
                       HttpServletResponse response) {
        // 验证用户认证状态
        String token = tokenByQuery;
        if (StringUtils.hasText(tokenByHeader)) {
            token = tokenByHeader;
        }
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
//            return RestResult.fail().message("用户未登录！");
            return;
        }

        // 获取文件路径
        UserFile userFileById = userFileService.getById(userFileId);
        String path = fileService.getById(userFileById.getFileId()).getFileUrl();
        File imgFile = FileUtil.file(path);
        try {
            FileInputStream fis = new FileInputStream(imgFile);
            response.reset();
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            IOUtils.copy(fis, response.getOutputStream());
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Operation(summary = "获取视频mpd", description = "获取视频", tags = {"file"})
    @GetMapping("/video/{id}/mpd")
    public ResponseEntity<org.springframework.core.io.Resource> getVideo(
            @PathVariable("id") long userFileId,
            @RequestHeader(name = "token", required = false) String tokenByHeader,
            String tokenByQuery
    ) {
        // 验证用户认证状态
        String token = tokenByQuery;
        if (StringUtils.hasText(tokenByHeader)) {
            token = tokenByHeader;
        }
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
//            return RestResult.fail().message("用户未登录！");
            return null;
        }

        // 获取视频路径
        UserFile userfile = userFileService.getById(userFileId);
        com.ziyiou.netshare.model.File fileById = fileService.getById(userfile.getFileId());
        String filepath = fileById.getFileUrl();

        // 将视频分片文件暂存到服务器目录下，定期清理
        String parent = FileUtil.getParent(filepath, 1);
        // 暂存到同级目录下，新建同名文件夹。
        String slicePath = parent + File.separator + fileById.getIdentifier() + File.separator;

        // 请求生成视频流文件
        if (!FileUtil.exist(slicePath) || FileUtil.isDirEmpty(new File(slicePath))) {
            // 分片目录不存在，执行视频初始化操作
            // 1、创建文件夹
            FileUtil.mkdir(slicePath);
            // 转视频流  ffmpeg -i "filepath" -c:v libx264 -c:a aac -f dash "slicePath/identifier.mpd"
            String cmd = "ffmpeg -i \"" + filepath + "\" -c copy -f dash \"" +
                    slicePath + fileById.getIdentifier() + ".mpd\"";
            Process process = RuntimeUtil.exec(null, new File(slicePath), cmd);

            /*
                https://www.cnblogs.com/badboyf/p/8366172.html
                Runtime.exec()执行时JVM会产生一个子进程，该进程与JVM建立三个通道链接：标准输入，标准输出，标准错误。
                Java本地的系统对标准输入和输出所提供的缓冲池有限，所以错误的对标准输出快速的写入和从标准输入快速的读入都有可能造成子进程死锁。
                子进程的输出流，也就是JVM的输入流。子进程不断向控制台输出，如果Java没有把输入流及时清空，会导致缓存区满，导致死锁。
                解决办法就是及时清空输入流，开两个线程把process.getInputStream()和process.getErrorStream()读出来就可以。
                对于ffmpeg只需要process.getErrorStream()读出来就可以了。
             */
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                while (br.readLine() != null) {
                }
                process.waitFor();
            } catch (Exception e) {
            }
        }

        // 若目录存在，则已经有分片，直接返回视频流
        // 生成本地文件资源
        org.springframework.core.io.Resource resource =
                new FileSystemResource(slicePath + fileById.getIdentifier() + ".mpd");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/dash+xml"));
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @Operation(summary = "获取视频片", description = "获取视频", tags = {"file"})
    @GetMapping("/video/{id}/{name}")
    public ResponseEntity<org.springframework.core.io.Resource> getVideoSlice(
            @PathVariable("name") String name,
            @PathVariable("id") String userFileId) {
        String slicePath = getSlicePath(userFileService.getById(userFileId));
        org.springframework.core.io.Resource resource;

        // 若目录存在，则已经有分片，直接返回视频流
        // 生成本地文件资源
        resource = new FileSystemResource(slicePath + name);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);

    }

    private String getSlicePath(UserFile userFileService) {
        org.springframework.core.io.Resource resource;
        // 获取视频路径
        UserFile userfile = userFileService;
        com.ziyiou.netshare.model.File fileById = fileService.getById(userfile.getFileId());
        String filepath = fileById.getFileUrl();

        // 将视频分片文件暂存到服务器目录下，定期清理
        String parent = FileUtil.getParent(filepath, 1);
        // 暂存到同级目录下，新建同名文件夹。
        String slicePath = parent + File.separator + fileById.getIdentifier() + File.separator;
        return slicePath;
    }

}

