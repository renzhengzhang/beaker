# 创建管理员用户，用于远程连接
create user if not exists 'admin'@'%' identified with caching_sha2_password by 'password';
grant all privileges on *.* to 'admin'@'%' with grant option;
grant process on *.* to 'admin'@'%' with grant option;
flush privileges;

# 创建数据库用户，用于应用连接
create user if not exists 'beaker'@'%' identified with mysql_native_password by 'password';
grant all on beaker.* to 'beaker'@'%';
grant process on *.* to 'beaker'@'%';
flush privileges;

# 创建数据库和表
create database if not exists `beaker` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
create table if not exists `beaker`.`user`
(
    `id`           bigint unsigned not null auto_increment,
    `created_at`   datetime        not null default current_timestamp comment '创建时间',
    `updated_at`   datetime        not null default current_timestamp comment '更新时间',
    `creator`      bigint unsigned not null comment '创建人',
    `modifier`     bigint unsigned not null comment '更新人',
    `deleted`      tinyint         not null default '0' comment '删除标识. 0: 未删除; 1: 已删除',
    `username`     varchar(64)     not null comment '用户名',
    `gender`       tinyint                  default null comment '性别. 0: 女, 1: 男',
    `birthday`     datetime                 default null comment '生日',
    `phone_number` char(11)                 default null comment '电话号码',
    `email`        varchar(256)             default null comment '电子邮件地址',
    `passwd`       varchar(128)    not null comment '密码',
    `banned`       tinyint                  default '0' comment '禁用标识. 0: 否, 1: 是',
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4
  collate = utf8mb4_0900_ai_ci;