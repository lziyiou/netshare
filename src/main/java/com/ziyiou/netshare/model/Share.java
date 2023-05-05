package com.ziyiou.netshare.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Table(name = "share")
@Entity
@TableName("share")
public class Share {
    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint(20) comment '分享id'")
    private Long shareId;

    @Column(columnDefinition = "bigint(30) comment '文件id'")
    private Long userFileId;

    @Column(columnDefinition = "varchar(35) comment '链接'")
    private String shareLink;

    @Column(columnDefinition = "bigint(15) comment '分享者id'")
    private Long userId;

    @Column(columnDefinition = "timestamp comment '生成链接时间'")
    private Date createTime;

    @Column(columnDefinition = "timestamp comment '有效时间 -1为永久'")
    private Integer expiration;

    @Column(columnDefinition = "timestamp comment '分享形式  0-私密   1-公开'")
    private Integer form;
}
