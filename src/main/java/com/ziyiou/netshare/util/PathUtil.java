package com.ziyiou.netshare.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@Slf4j
public class PathUtil {
    public static String getSaveFilePath() {
        // 存放目录：     root/upload/date
        String path = "upload";
        String dateStr = DateUtil.today().replace("-", "");
        path = File.separator + path + File.separator + dateStr;

        String staticPath = PathUtil.getStaticPath();

        // 如果目录不存在，创建目录
        return FileUtil.mkdir(staticPath + path).getPath();
    }

    public static String getStaticPath() {
        // 查看是否指定了存储位置
        String localStoragePath = PropertiesUtil.getProperty("file.local-storage-path");
        if (StringUtils.isNotEmpty(localStoragePath)) {
            return localStoragePath;
        }else {
            String projectRootAbsolutePath = null;
            try {
                projectRootAbsolutePath = ResourceUtils.getURL("classpath:").getPath();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            int index = projectRootAbsolutePath.indexOf("file:");
            if (index != -1) {
                projectRootAbsolutePath = projectRootAbsolutePath.substring(0, index);
            }

            return projectRootAbsolutePath + "static" + File.separator;
        }
    }

}
