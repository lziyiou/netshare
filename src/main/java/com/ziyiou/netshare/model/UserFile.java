package com.ziyiou.netshare.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Table(name = "userfile", uniqueConstraints = {
        @UniqueConstraint(name = "fileindex", columnNames = {
                "filename", "filepath", "extendName"
        })
})
@Entity
@TableName("userfile")
public class UserFile {
    @Id
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint(20) comment '用户文件id'")
    private Long userFileId;

    @Column(columnDefinition = "bigint(20) comment '用户id'")
    private Long userId;

    @Column(columnDefinition = "bigint(20) comment '文件id'")
    private Long fileId;

    @Column(columnDefinition="varchar(100) comment '文件名'")
    private String filename;

    @Column(columnDefinition="varchar(500) comment '文件路径'")
    private String filepath;

    @Column(columnDefinition="varchar(100) comment '扩展名'")
    private String extendName;

    @Column(columnDefinition="int(1) comment '是否是目录 0-否, 1-是'")
    private Integer isDir;

    @Column(columnDefinition="timestamp comment '上传时间'")
    private Date uploadTime;
}
