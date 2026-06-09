USE supermarket_db;

-- 清空旧数据（注意外键顺序：先删明细，再删主表）
DELETE FROM purchase_detail;
DELETE FROM purchase_main;
DELETE FROM member;
DELETE FROM employee;
DELETE FROM goods;
DELETE FROM supplier;

-- ==================== 供应商 ====================
INSERT INTO supplier (name, short_name, address, phone, email, contact_person, contact_phone, remark) VALUES
('华为技术有限公司', '华为', '深圳市龙岗区坂田华为基地', '0755-28780808', 'huawei@huawei.com', '张三', '13800138001', '主要供应商'),
('阿里巴巴集团', '阿里', '杭州市余杭区文一西路969号', '0571-85022088', 'alibaba@alibaba.com', '李四', '13800138002', '云服务供应商'),
('腾讯科技有限公司', '腾讯', '深圳市南山区科技园', '0755-86013388', 'tencent@tencent.com', '王五', '13800138003', NULL),
('京东集团', '京东', '北京市亦庄经济开发区', '010-89118888', 'jd@jd.com', '赵六', '13800138004', '物流合作商'),
('小米科技', '小米', '北京市海淀区清河', '010-60606666', 'xiaomi@xiaomi.com', '孙七', '13800138005', '智能硬件供应商');

-- ==================== 商品 ====================
INSERT INTO goods (name, price, supplier_id, description, remark) VALUES
('华为Mate 60 Pro', 6999.00, 1, '智能手机', '热销产品'),
('华为P50 Pocket', 4988.00, 1, '折叠屏手机', NULL),
('华为平板MatePad', 2999.00, 1, '平板电脑', '教育优惠'),
('天猫精灵', 99.00, 2, '智能音箱', '入门款'),
('腾讯视频会员年卡', 168.00, 3, '数字商品', '自动充值'),
('京东E卡500元', 500.00, 4, '电子礼品卡', '全国通用'),
('小米手环8 Pro', 399.00, 5, '智能穿戴设备', '新品上市'),
('小米充电宝', 79.00, 5, '移动电源20000mAh', NULL);

-- ==================== 员工（密码为 123456，已用 SHA-256 + Base64 加密）====================
INSERT INTO employee (name, password, level, phone, salary, remark, is_approved) VALUES
('管理员', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', 2, '13800000001', 15000.00, '系统管理员', 1),
('普通员工', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', 1, '13800000002', 8000.00, '普通员工', 1);

-- ==================== 会员 ====================
INSERT INTO member (name, phone, email, points, level, register_time, remark) VALUES
('张三', '13800138006', 'zhangsan@email.com', 1000, 2, '2025-01-15 10:00:00', '银卡会员'),
('李四', '13800138007', 'lisi@email.com', 500, 1, '2025-02-20 14:30:00', NULL),
('王五', '13800138008', 'wangwu@email.com', 3000, 3, '2024-11-01 09:00:00', '金卡会员'),
('赵六', '13800138009', 'zhaoliu@email.com', 5000, 4, '2024-06-18 16:20:00', '钻石会员'),
('孙七', '13800138010', NULL, 200, 1, '2026-03-10 11:45:00', '新注册会员');

-- ==================== 采购主表 ====================
INSERT INTO purchase_main (order_no, employee_id, total_quantity, total_price, purchase_time, status, deadline_time, remark) VALUES
('PO-20260609-001', 1, 50, 200530.00, '2026-06-09 09:00:00', 'DRAFT', '2026-06-09 17:00:00', '华为设备采购'),
('PO-20260608-001', 1, 80, 16920.00, '2026-06-08 10:30:00', 'HISTORY', '2026-06-08 17:00:00', '日常补货');

-- ==================== 采购明细 ====================
INSERT INTO purchase_detail (purchase_main_id, goods_id, quantity, unit_price, total_price, remark) VALUES
(1, 1, 10, 6999.00, 69990.00, 'Mate 60 Pro'),
(1, 2, 20, 4988.00, 99760.00, 'P50 Pocket'),
(1, 3, 10, 2999.00, 29990.00, 'MatePad'),
(1, 8, 10, 79.00, 790.00, '充电宝'),
(2, 4, 50, 99.00, 4950.00, '天猫精灵'),
(2, 7, 30, 399.00, 11970.00, '手环');
