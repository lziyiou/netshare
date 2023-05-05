package com.ziyiou.netshare.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateBetween;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.model.File;
import com.ziyiou.netshare.model.Share;
import com.ziyiou.netshare.model.User;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.ShareFileDTO;
import com.ziyiou.netshare.model.dto.UserFileListDTO;
import com.ziyiou.netshare.model.vo.UserFileListVO;
import com.ziyiou.netshare.service.FileService;
import com.ziyiou.netshare.service.ShareService;
import com.ziyiou.netshare.service.UserFileService;
import com.ziyiou.netshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "share", description = "该接口为分享文件接口，主要用来做一些文件的分享链接生成，链接请求访问等。")
@RestController
@Slf4j
public class ShareController {
    @Resource
    UserFileService userFileService;
    @Resource
    UserService userService;
    @Resource
    private ShareService shareService;
    @Resource
    private FileService fileService;

    @Operation(summary = "生成分享文件链接", description = "生成分享文件链接", tags = {"share"})
    @PostMapping("/share")
    public RestResult generateShareLink(@RequestHeader("token") String token,
                                        @RequestBody ShareFileDTO shareFileDTO) {
        // 验证用户认证状态
        User userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            return RestResult.fail().message("用户未登录！");
        }

        String link = userFileService.generateShareLink(shareFileDTO);

        return RestResult.success().message("创建分享链接成功！").data(link);
    }

    @Operation(summary = "获取分享文件", description = "获取分享文件", tags = {"share"})
    @GetMapping("/s/{link}")
    public RestResult getShareFile(@PathVariable String link,
                                   UserFileListDTO userFileListDTO) {

        // 获取分享信息
        LambdaQueryWrapper<Share> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(Share::getShareLink, link);
        Share share = shareService.getOne(lambdaQueryWrapper);

        // 是否有链接信息
        if (share == null) {
            return RestResult.fail().message("分享不存在");
        }

        // 判断链接是否已过期
        if (share.getExpiration() != -1) {
            DateTime expDay = DateUtil.offsetDay(share.getCreateTime(), share.getExpiration());
            if (expDay.isBefore(DateTime.now())) {
                return RestResult.fail().message("分享已过期");
            }
        }

        // 获取分享文件
        LambdaQueryWrapper<UserFile> userFileLambdaQWrapper = new LambdaQueryWrapper<>();
        userFileLambdaQWrapper
                .eq(UserFile::getUserId, share.getUserId())
                .eq(UserFile::getUserFileId, share.getUserFileId());
        UserFile userFile = userFileService.getOne(userFileLambdaQWrapper);


        List<UserFileListVO> fileList = new ArrayList<>();
        long total = 0;
        if (Objects.equals(userFileListDTO.getFilepath(), "/")) {
            UserFileListVO userFileListVO = BeanUtil.toBean(userFile, UserFileListVO.class);
            File file = fileService.getById(userFile.getFileId());
            BeanUtil.copyProperties(file, userFileListVO);
            fileList.add(userFileListVO);
            total = 1; // 目前只有单文件或单文件夹分享，所以根目录只会有一条记录
        } else {
            String path = userFile.getFilepath() + userFileListDTO.getFilepath().substring(1);
            fileList = userFileService.getUserFileByFilepath(path, userFile.getUserId(),
                    userFileListDTO.getCurrentPage(), userFileListDTO.getPageCount());

            // 查询数据总数量
            userFileLambdaQWrapper.clear();
            userFileLambdaQWrapper.eq(UserFile::getUserId, userFile.getUserId())
                    .eq(UserFile::getFilepath, path);
            total = userFileService.count(userFileLambdaQWrapper);
        }

        // 获取分享者信息
        User shareUser = userService.getById(userFile.getUserId());
        shareUser.setPassword(null);
        shareUser.setTelephone(null);
        shareUser.setSalt(null);

        // 返回数据
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("list", fileList);
        map.put("shareUser", shareUser);
        DateTime expDay = DateUtil.offsetDay(share.getCreateTime(), share.getExpiration());
        DateBetween between = expDay.between(DateTime.now());
        map.put("remainder",
                between.between(DateUnit.DAY) + "天" + between.between(DateUnit.HOUR) % 24 + "小时");    // 剩余时间


        return RestResult.success().data(map);
    }
}
