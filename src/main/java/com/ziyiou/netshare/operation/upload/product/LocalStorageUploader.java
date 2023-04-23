package com.ziyiou.netshare.operation.upload.product;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import com.ziyiou.netshare.operation.upload.Uploader;
import com.ziyiou.netshare.operation.upload.domain.UploadFile;
import com.ziyiou.netshare.util.PropertiesUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

@Component
public class LocalStorageUploader extends Uploader {

    public LocalStorageUploader() {

    }

    // 上传一个文件 本地保存
    @Override
    public String upload(MultipartFile multipartFile, UploadFile uploadFile) {
        String rootPath = PropertiesUtil.getProperty("file.local-path");
        // 根目录不存在时 创建
        FileUtil.mkdir(rootPath);

        // 文件按日期分文件夹
        String dirPath =
                rootPath + DateTime.now().toString("yyyyMMdd") + File.separator;
        // 分片目录 合并后删除
        String chunkPath = dirPath + uploadFile.getIdentifier() + File.separator;
        FileUtil.mkdir(chunkPath);
        // 分片名
        String chunkFilepath = chunkPath + uploadFile.getIdentifier() + uploadFile.getChunkNumber();

        try {
            multipartFile.transferTo(new File(chunkFilepath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String fpath = "";
        // 最后一个分片上传完成后 自动合并文件
        if (uploadFile.getChunkNumber() == uploadFile.getTotalChunks()) {
            fpath = dirPath + uploadFile.getIdentifier() + multipartFile.getOriginalFilename();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 1; i <= uploadFile.getTotalChunks(); i++) {
                list.add(chunkPath + uploadFile.getIdentifier() + i);
            }
            merge2TargetFileDeleteSourceFile(fpath, list);
            // 合并完删除分片目录
            FileUtil.del(chunkPath);
        }

        return fpath;
    }

    /**
     * 合并文件ArrayList中的多个源文件为一个文件.
     *
     * @param targetFilePath     目标文件路径名称字符串.
     * @param sourceFilePathList 存放源文件的路径名称字符串的ArrayList集合.
     */
    public void merge2TargetFileDeleteSourceFile(String targetFilePath,
                                                 ArrayList<String> sourceFilePathList) {
        // 如果ArrayList中有东西
        if (sourceFilePathList.size() > 0) {
            BufferedInputStream in;
            try (BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(targetFilePath));) {
                for (Iterator<String> iterator = sourceFilePathList
                        .iterator(); iterator.hasNext(); ) {
                    String sourceFilePath = iterator.next();
                    File sourceFile = new File(sourceFilePath);
                    in = new BufferedInputStream(
                            new FileInputStream(sourceFile));
                    // 缓存数组
                    byte[] buffer = new byte[2048];
                    // 每次读入的字节数量
                    int inSize = -1;
                    // 批量读入字节到buffer缓存中,并返回读入的自己数量给inSize
                    while ((inSize = in.read(buffer)) != -1) {
                        // 把buffer缓存中的字节写入输出流(也就是目标文件)
                        out.write(buffer, 0, inSize);
                    }
                    // 关闭源文件
                    in.close();
                    // 删除这个源文件
                    sourceFile.delete();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
