package com.ziyiou.netshare.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.ziyiou.netshare.constant.FileConstant;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.service.FileService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@Tag(name = "media", description = "多媒体文件的返回")
@RestController
@Slf4j
public class MediaStreamController {
    @jakarta.annotation.Resource
    FileService fileService;
    @jakarta.annotation.Resource
    UserService userService;
    @jakarta.annotation.Resource
    UserFileService userFileService;

    @Operation(summary = "获取图片", description = "获取图片", tags = {"media"})
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


    @Operation(summary = "获取视频mpd", description = "获取视频", tags = {"media"})
    @GetMapping("/video/{id}/mpd")
    public ResponseEntity<Resource> getVideo(
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
        String slicePath = parent + FileConstant.FILE_SEPARATOR + fileById.getIdentifier() + FileConstant.FILE_SEPARATOR;

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

    @Operation(summary = "获取视频片", description = "获取视频", tags = {"media"})
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
        String slicePath = parent + FileConstant.FILE_SEPARATOR + fileById.getIdentifier() + FileConstant.FILE_SEPARATOR;
        return slicePath;
    }
}
