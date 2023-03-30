package com.ziyiou.netshare.advice;

import com.ziyiou.netshare.common.RestResult;
import com.ziyiou.netshare.common.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlerAdvice {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public <T> RestResult<T> error(Exception e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.fail();
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    //空指针处理方法
    public <T> RestResult<T> error(NullPointerException e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.setResult(ResultCodeEnum.NULL_POINT);
    }

    //下标越界处理方法
    @ExceptionHandler(IndexOutOfBoundsException.class)
    @ResponseBody
    public <T> RestResult<T> error(IndexOutOfBoundsException e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.setResult(ResultCodeEnum.INDEX_OUT_OF_BOUNDS);
    }
}
