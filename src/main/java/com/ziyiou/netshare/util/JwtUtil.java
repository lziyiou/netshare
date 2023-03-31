package com.ziyiou.netshare.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import com.ziyiou.netshare.constant.JwtConstant;
import com.ziyiou.netshare.model.User;

public class JwtUtil {
    /**
     * 创建jwt
     */
    public static String createJWT(User subject) {
        // 生成JWT的时间
        DateTime nowTime = DateUtil.date();

        // 为payload添加各种标准声明和私有声明
        return JWT.create()
                .setPayload("sub", subject)
                .setExpiresAt(DateTime.of(System.currentTimeMillis() + JwtConstant.exp))
                .setIssuer(JwtConstant.iss)
                .setAudience(JwtConstant.aud)
                .setKey(JwtConstant.secret.getBytes())
                .setIssuedAt(nowTime)
                .sign();
    }

}
