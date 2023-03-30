package com.ziyiou.netshare.exception;

public class NotSameFileException extends Exception {
    public NotSameFileException() {
        super("File MD5 Different");
    }
}
