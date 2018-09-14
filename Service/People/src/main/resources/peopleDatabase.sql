CREATE TABLE t_people(
id VARCHAR(32) PRIMARY KEY NOT NULL COMMENT '人员全局ID',
name VARCHAR(10) NOT NULL COMMENT '人员姓名',
idcard VARCHAR(18) NOT NULL COMMENT '身份证',
region BIGINT(20) NOT NULL COMMENT '区域ID(省市区)',
household VARCHAR(100) COMMENT '户籍',
address VARCHAR(100) COMMENT '现居地',
sex VARCHAR(2) DEFAULT 0 COMMENT '性别',
age INT(2) COMMENT '年龄',
birthday VARCHAR(10) COMMENT '出生日期',
politic VARCHAR(10) COMMENT '政治面貌',
edulevel VARCHAR(10) COMMENT '文化程度',
job VARCHAR(10) COMMENT '职业',
birthplace VARCHAR(10) COMMENT '籍贯',
community BIGINT(20) COMMENT '小区ID',
important INT(2) DEFAULT 0 COMMENT '重点关注',
care INT(2) DEFAULT 0 COMMENT '关爱人口',
lasttime TIMESTAMP COMMENT '最后出现时间',
createtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updatetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
UNIQUE KEY (id),
UNIQUE KEY (idcard)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '人口库';


CREATE TABLE t_flag(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
flagid INT(2) NOT NULL COMMENT '标签ID',
flag VARCHAR(10) NOT NULL COMMENT '标签',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '标签表';


CREATE TABLE t_picture(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
idcardpic LONGBLOB COMMENT '证件照片',
capturepic LONGBLOB COMMENT '实际采集照片',
feature VARCHAR(8192) NOT NULL COMMENT '特征值',
bitfeature VARCHAR(512) NOT NULL COMMENT 'bit特征值',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '照片信息库';


CREATE TABLE t_imsi(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
imsi VARCHAR(20) NOT NULL COMMENT 'IMSI码',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'IMSI码表';


CREATE TABLE t_phone(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
phone VARCHAR(15) NOT NULL COMMENT '联系方式',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '联系方式表';


CREATE TABLE t_house(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
house VARCHAR(100) NOT NULL COMMENT '房产信息',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '房产信息表';


CREATE TABLE t_car(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
car VARCHAR(100) NOT NULL COMMENT '车辆信息',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '车辆信息表';


CREATE TABLE t_newpeople(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
community BIGINT(20) NOT NULL COMMENT '小区id',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '新增记录查询表';


CREATE TABLE t_recognize(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
deviceid VARCHAR(50) NOT NULL COMMENT '设备ID',
capturetime TIMESTAMP NOT NULL COMMENT '抓拍时间',
surl VARCHAR(255) NOT NULL COMMENT '小图ftp路径（带ip的ftpurl）',
burl VARCHAR(255) NOT NULL COMMENT '大图ftp路径（带ip的ftpurl）',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '识别记录表';


CREATE TABLE t_fusion_imsi(
id BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT 'ID',
peopleid VARCHAR(32) NOT NULL COMMENT '人员全局ID',
receivetime TIMESTAMP NOT NULL COMMENT '接收时间',
address VARCHAR(100) NOT NULL COMMENT '设备地址',
imsi VARCHAR(20) NOT NULL COMMENT 'imsi码',
UNIQUE KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '数据融合记录表';