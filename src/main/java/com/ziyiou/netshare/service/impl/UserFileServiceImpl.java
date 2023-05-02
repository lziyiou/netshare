package com.ziyiou.netshare.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.constant.FileConstant;
import com.ziyiou.netshare.mapper.UserFileMapper;
import com.ziyiou.netshare.model.UserFile;
import com.ziyiou.netshare.model.dto.MoveFileDTO;
import com.ziyiou.netshare.model.vo.UserFileListVO;
import com.ziyiou.netshare.service.UserFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements UserFileService {
    @Resource
    UserFileMapper userFileMapper;

    @Override
    public List<UserFileListVO> getUserFileByFilePath(String filepath, Long userId, Long currentPage, Long pageCount) {
        // 计算查询的页码数
        Long beginCount = (currentPage - 1) * pageCount;
        // 查询数据
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setFilepath(filepath);
        return userFileMapper.userFileList(userFile, beginCount, pageCount);
    }

    @Override
    public Map<String, Object> getUserFileByType(int filetype, Long currentPage, Long pageCount, Long userId) {
        // 计算查询的条数页码
        Long beginCount = (currentPage - 1) * pageCount;

        List<UserFileListVO> fileList = null; // 查询的数据结果
        Long total = 0L;    // 结果的条数
        List<String> arrList = null;
        switch (filetype) {
            case FileConstant.OTHER_TYPE -> {
                arrList = new ArrayList<>();
                arrList.addAll(Arrays.asList(FileConstant.DOC_FILE));
                arrList.addAll(Arrays.asList(FileConstant.IMG_FILE));
                arrList.addAll(Arrays.asList(FileConstant.VIDEO_FILE));
                arrList.addAll(Arrays.asList(FileConstant.MUSIC_FILE));
                // 其他类型的查询数据库
                fileList = userFileMapper.selectFileNotInExtendName(arrList, beginCount, pageCount, userId);
                total = userFileMapper.selectCountNotInExtendName(arrList, beginCount, pageCount, userId);
            }
            case FileConstant.IMAGE_TYPE -> arrList = Arrays.asList(FileConstant.IMG_FILE);
            case FileConstant.DOC_TYPE -> arrList = Arrays.asList(FileConstant.DOC_FILE);
            case FileConstant.VIDEO_TYPE -> arrList = Arrays.asList(FileConstant.VIDEO_FILE);
            case FileConstant.MUSIC_TYPE -> arrList = Arrays.asList(FileConstant.MUSIC_FILE);
        }
        // 指定类型的查询数据库
        if (filetype != FileConstant.OTHER_TYPE) {
            fileList = userFileMapper.selectFileByExtendName(arrList, beginCount, pageCount, userId);
            total = userFileMapper.selectCountByExtendName(arrList, beginCount, pageCount, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", fileList);
        map.put("total", total);
        return map;
    }

    @Override
    public RestResult rename(Long userFileId, String newName) {

        // 检查当前用户下是否有同目录下的文件名
        UserFile uf = userFileMapper.selectById(userFileId);
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFilename, newName)
                .eq(UserFile::getFilepath, uf.getFilepath())
                .eq(UserFile::getUserId, uf.getUserId());
        List<UserFile> userFileList = this.list(lambdaQueryWrapper);
        if (!userFileList.isEmpty()) {
            return RestResult.fail().message("同目录下文件名重复");
        }

        // 修改子级文件路径
        if (uf.getIsDir() == 1) {
            // 查出子级文件并修改路径
            lambdaQueryWrapper.clear();
            lambdaQueryWrapper.likeRight(UserFile::getFilepath,
                    uf.getFilepath() + uf.getFilename());
            userFileList = this.list(lambdaQueryWrapper);
            userFileList.forEach((item)->{
                item.setFilepath(item.getFilepath().replace(uf.getFilepath()+uf.getFilename()+"/",
                        uf.getFilepath()+newName+"/"));
                this.updateById(item);
            });

        }

            uf.setUserFileId(userFileId);
        uf.setFilename(newName);
        userFileMapper.updateById(uf);

        return RestResult.success().data(uf);
    }

    @Override
    public JSONArray getDirTree(Long userId) {
        JSONArray dirTree = JSONUtil.createArray();

        // 添加根目录
        JSONObject rootData = JSONUtil.createObj();
        rootData.set("label", "/");
        rootData.set("value", 0);
        JSONArray children = JSONUtil.createArray();
        rootData.set("children", children);
        dirTree.put(rootData);

        appendDir(rootData, 0, userId);

        return dirTree;
    }

    @Override
    public void moveFile(MoveFileDTO moveFileDTO) {
        // 1、根据userFileId查到文件或目录
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(UserFile::getUserFileId, moveFileDTO.getUserFileId());
        UserFile userFile = this.getOne(lambdaQueryWrapper);

        // 更新数据库
        userFile.setParentId(moveFileDTO.getParentId());
        // 查出新父路径
        lambdaQueryWrapper.clear();
        lambdaQueryWrapper
                .select(UserFile::getFilename, UserFile::getFilepath)
                .eq(UserFile::getUserFileId, moveFileDTO.getParentId())
                .eq(UserFile::getUserId, userFile.getUserId());
        UserFile parent = this.getOne(lambdaQueryWrapper);

        String filepath = parent.getFilepath() + parent.getFilename() + FileConstant.FILE_SEPARATOR;

        // 记录旧路径
        String oldParentPath = userFile.getFilepath();
        // 更新为新路径
        userFile.setFilepath(filepath);
        this.saveOrUpdate(userFile);


        // 如果是文件夹需要多进行一步子级修改
        if (userFile.getIsDir() == 1) {
            // 3、如果是文件夹，修改本身和子级所有的filepath、parentId
            // 1) 查出所有子级目录
            lambdaQueryWrapper.clear();
            lambdaQueryWrapper.likeRight(UserFile::getFilepath,oldParentPath + userFile.getFilename())
                    .eq(UserFile::getUserId, userFile.getUserId());
            List<UserFile> children = list(lambdaQueryWrapper);

            children.forEach(item -> {
                // 先查出原目录，只改掉需要改的前半部分   filepath
                // 子目录不需修改父id
                String oldPath = item.getFilepath();
                String path = oldPath.replace(oldParentPath, userFile.getFilepath());
                item.setFilepath(path);
                this.saveOrUpdate(item);
            });
        }

    }

    private void appendDir(JSONObject rootData, long parentId, Long userId) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getIsDir, 1)
                .eq(UserFile::getParentId, parentId);
        List<UserFile> dirs = this.list(lambdaQueryWrapper);
        // 遍历当前目录，递归加入子级目录
        dirs.forEach(item -> {
            JSONObject obj = JSONUtil.createObj();
            obj.set("label", item.getFilename());
            obj.set("value", item.getUserFileId());
            JSONArray objc = JSONUtil.createArray();
            obj.set("children", objc);
            rootData.append("children", obj);

            // 递归子级
            appendDir(obj, item.getUserFileId(), userId);
        });

    }


}
