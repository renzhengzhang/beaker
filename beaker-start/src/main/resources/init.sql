CREATE DATABASE `beaker` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */

CREATE TABLE `user`
(
    `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
    `created_at`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `creator`      bigint unsigned NOT NULL COMMENT '创建人',
    `modifier`     bigint unsigned NOT NULL COMMENT '更新人',
    `deleted`      tinyint     NOT NULL DEFAULT '0' COMMENT '删除标识. 0: 未删除; 1: 已删除',
    `username`     varchar(64) NOT NULL COMMENT '用户名',
    `gender`       tinyint              DEFAULT NULL COMMENT '性别. 0: 女, 1: 男',
    `birthday`     datetime             DEFAULT NULL COMMENT '生日',
    `phone_number` char(11)             DEFAULT NULL COMMENT '电话号码',
    `email`        varchar(256)         DEFAULT NULL COMMENT '电子邮件地址',
    `passwd`       char(64)    NOT NULL COMMENT '密码',
    `baned`        tinyint              DEFAULT '0' COMMENT '禁用标识. 0: 否, 1: 是',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;