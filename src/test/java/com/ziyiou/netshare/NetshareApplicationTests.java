package com.ziyiou.netshare;

import com.ziyiou.netshare.mapper.UserMapper;
import com.ziyiou.netshare.util.PathUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NetshareApplicationTests {
    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
    }

    @Test
    public void test() {
        System.out.println(PathUtil.getSaveFilePath());
    }


}
