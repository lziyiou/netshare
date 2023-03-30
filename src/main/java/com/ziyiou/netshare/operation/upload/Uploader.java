package com.ziyiou.netshare.operation.upload;

import cn.hutool.core.io.FileUtil;
import com.ziyiou.netshare.operation.upload.domain.UploadFile;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

@Slf4j
public abstract class Uploader {
    public abstract List<UploadFile> upload(HttpServletRequest request, UploadFile uploadFile);

//    /**
//     * 根据字符串创建本地目录 并按照日期建立子目录返回
//     *
//     * @return
//     */
//    protected String getSaveFilePath() {
//
//        String path = ROOT_PATH;
//        SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
//        path = FILE_SEPARATOR + path + FILE_SEPARATOR + formater.format(new Date());
//
//        String staticPath = PathUtil.getStaticPath();
//
//        File dir = new File(staticPath + path);
//        //LOG.error(PathUtil.getStaticPath() + path);
//        if (!dir.exists()) {
//            try {
//                boolean isSuccessMakeDir = dir.mkdirs();
//                if (!isSuccessMakeDir) {
//                    log.error("目录创建失败:" + PathUtil.getStaticPath() + path);
//                }
//            } catch (Exception e) {
//                log.error("目录创建失败" + PathUtil.getStaticPath() + path);
//                return "";
//            }
//        }
//        return path;
//    }
//    /**
//     * 依据原始文件名生成新文件名
//     *
//     * @return
//     */
//    protected String getTimeStampName() {
//        try {
//            SecureRandom number = SecureRandom.getInstance("SHA1PRNG");
//            return "" + number.nextInt(10000)
//                    + System.currentTimeMillis();
//        } catch (NoSuchAlgorithmException e) {
//            log.error("生成安全随机数失败");
//        }
//        return ""
//                + System.currentTimeMillis();
//
//    }
    public synchronized boolean checkUploadStatus(UploadFile param, File confFile) throws IOException {
        RandomAccessFile confAccessFile = new RandomAccessFile(confFile, "rw");
        //设置文件长度
        confAccessFile.setLength(param.getTotalChunks());
        //设置起始偏移量
        confAccessFile.seek(param.getChunkNumber() - 1);
        //将指定的一个字节写入文件中 127，
        confAccessFile.write(Byte.MAX_VALUE);
//        byte[] completeStatusList = FileUtils.readFileToByteArray(confFile);
        byte[] completeStatusList = FileUtil.readBytes(confFile);
        confAccessFile.close();//不关闭会造成无法占用
        //创建conf文件文件长度为总分片数，每上传一个分块即向conf文件中写入一个127，那么没上传的位置就是默认的0,已上传的就是127
        for (int i = 0; i < completeStatusList.length; i++) {
            if (completeStatusList[i] != Byte.MAX_VALUE) {
                return false;
            }
        }
        FileUtil.del(confFile);
        return true;
    }

}
