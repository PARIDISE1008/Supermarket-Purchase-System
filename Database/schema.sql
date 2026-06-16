CREATE DATABASE IF NOT EXISTS supermarket_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE supermarket_db;

DROP TABLE IF EXISTS purchase_detail;
DROP TABLE IF EXISTS purchase_main;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS goods;
DROP TABLE IF EXISTS supplier;

CREATE TABLE supplier(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '供应商编号',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    short_name VARCHAR(50) COMMENT '简称',
    address VARCHAR(200)  COMMENT '地址',
    phone VARCHAR(20) NOT NULL COMMENT '电话',
    email VARCHAR(100) COMMENT '邮件',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系人电话',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除(0-正常, 1-已删除)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT uq_supplier_phone UNIQUE KEY(phone),
    INDEX idx_supplier_name(name),
    INDEX idx_supplier_is_deleted(is_deleted)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '供应商表';



CREATE TABLE goods(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品编号',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    supplier_id INT NOT NULL COMMENT '供应商编号',
    description VARCHAR(500) COMMENT '简介' ,
    remark VARCHAR(500) COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除(0-正常, 1-已删除)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT uq_goods_supplier_name UNIQUE KEY(supplier_id, name),
    INDEX idx_goods_supplier(supplier_id),
    INDEX idx_goods_name(name),
    INDEX idx_goods_price(price),
    INDEX idx_goods_is_deleted(is_deleted),

    CONSTRAINT fk_goods_supplier
        FOREIGN KEY(supplier_id) REFERENCES supplier(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';



CREATE TABLE employee(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '员工编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    password VARCHAR(255) NOT NULL DEFAULT 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI='
        COMMENT '密码(SHA-256+Base64加密, 默认密码123456)',
    level TINYINT DEFAULT 1 COMMENT '级别(1-普通员工, 2-管理员)',
    phone VARCHAR(20) COMMENT '电话',
    salary DECIMAL(10,2) COMMENT '工资',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除(0-在职, 1-离职)',
    is_approved TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态(0-待审批, 1-已通过)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT uq_employee_phone UNIQUE KEY(phone),
    INDEX idx_employee_name(name),
    INDEX idx_employee_level(level),
    INDEX idx_employee_is_deleted(is_deleted),
    INDEX idx_employee_is_approved(is_approved)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工表';



CREATE TABLE member(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '会员编号',
    name VARCHAR(50) NOT NULL COMMENT '会员姓名',
    phone VARCHAR(20) NOT NULL COMMENT '会员电话',
    email VARCHAR(100) COMMENT '会员邮箱',
    points INT NOT NULL DEFAULT 0 COMMENT '积分',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '会员等级(1-普通, 2-银卡, 3-金卡, 4-钻石)',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否逻辑删除(0-正常, 1-已注销)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT uq_member_phone UNIQUE KEY(phone),
    INDEX idx_member_name(name),
    INDEX idx_member_level(level),
    INDEX idx_member_is_deleted(is_deleted),
    INDEX idx_member_points(points)
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会员表';


CREATE TABLE purchase_main(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '内部主键',
    order_no VARCHAR(50) NOT NULL COMMENT '采购清单号(格式:PO-YYYYMMDD-XXX)',
    employee_id INT NOT NULL COMMENT '采购员编号',
    total_quantity INT NOT NULL DEFAULT 0 COMMENT '采购总数量',
    total_price DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '采购总价',
    purchase_time DATETIME NOT NULL COMMENT '采购时间',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT(草稿), HISTORY(已截止), CANCELLED(已作废)',
    deadline_time DATETIME COMMENT '截止时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT uq_purchase_order_no UNIQUE KEY(order_no),
    INDEX idx_purchase_employee_time(employee_id, purchase_time),
    INDEX idx_employee_date(employee_id, purchase_time),
    INDEX idx_purchase_employee(employee_id),
    INDEX idx_purchase_time(purchase_time),

    CONSTRAINT fk_purchase_employee
        FOREIGN KEY(employee_id) REFERENCES employee(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '采购主表';


CREATE TABLE purchase_detail(
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '采购明细号',
    purchase_main_id INT NOT NULL COMMENT '采购主表ID',
    goods_id INT NOT NULL COMMENT '商品编号',
    quantity INT NOT NULL COMMENT '采购数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '商品单价(冗余，记录采购时价格)',
    total_price DECIMAL(12,2) NOT NULL COMMENT '商品总价(quantity * unit_price)',
    remark VARCHAR(500) COMMENT '备注',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_detail_main(purchase_main_id),
    INDEX idx_detail_goods(goods_id),
    INDEX idx_detail_main_goods(purchase_main_id, goods_id),

    CONSTRAINT fk_detail_main
        FOREIGN KEY(purchase_main_id) REFERENCES purchase_main(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_detail_goods
        FOREIGN KEY(goods_id) REFERENCES goods(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '采购明细表';
