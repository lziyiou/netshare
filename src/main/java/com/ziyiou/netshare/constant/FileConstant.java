package com.ziyiou.netshare.constant;

public interface FileConstant {
    String[] IMG_FILE = {"bmp", "jpg", "png", "tif", "gif", "jpeg"};
    String[] DOC_FILE = {"doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "hlp", "wps", "rtf", "html", "pdf"};
    String[] VIDEO_FILE = {"avi", "mp4", "mpg", "mov", "swf"};
    String[] MUSIC_FILE = {"wav", "aif", "au", "mp3", "ram", "wma", "mmf", "amr", "aac", "flac"};
    int IMAGE_TYPE = 1;
    int DOC_TYPE = 2;
    int VIDEO_TYPE = 3;
    int MUSIC_TYPE = 4;
    int OTHER_TYPE = 5;
    int SHARE_FILE = 6;
    int RECYCLE_FILE = 7;


    String FILE_SEPARATOR = "/";
}
