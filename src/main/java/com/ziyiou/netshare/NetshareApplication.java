package com.ziyiou.netshare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ziyiou.netshare.mapper")
public class NetshareApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetshareApplication.class, args);
    }

}
