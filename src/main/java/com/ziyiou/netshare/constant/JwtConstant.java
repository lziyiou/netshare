package com.ziyiou.netshare.constant;

public interface JwtConstant {
    // 密钥
    String secret = "6L6T5LqG5L2g77yM6LWi5LqG5LiW55WM5Y+I6IO95aaC5L2V44CC";

    // 签名算法：HS256,HS384,HS512,RS256,RS384,RS512,ES256,ES384,ES512,PS256,PS384,PS512
    String alg = "HS256";

    // jwt签发者
    String iss = "ziyiou";

    //jwt过期时间（单位：毫秒）
    int exp = 60 * 60 * 1000 * 24 * 7;

    // jwt接收者
    String aud = "netshare";
}
