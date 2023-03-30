package com.ziyiou.netshare.util;

import cn.hutool.core.io.FileTypeUtil;
import com.ziyiou.netshare.constant.FileConstant;

public class FileUtil {
    /**
     * 判断是否为图片文件
     * @param path 文件路径
     */
    public static boolean isImageFile(String path) {
        String type = FileTypeUtil.getTypeByPath(path);
        for (int i = 0; i < FileConstant.IMG_FILE.length; i++) {
            if (type.equalsIgnoreCase(FileConstant.IMG_FILE[i])) {
                return true;
            }
        }
        return false;
    }



}
