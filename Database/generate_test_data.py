import hashlib
import base64
import random
import os

def sha256_encode(raw):
    digest = hashlib.sha256(raw.encode('utf-8')).digest()
    return base64.b64encode(digest).decode('ascii')

PASSWORD_HASH = sha256_encode("123456")

def unique_phones(count, start=13800000000):
    phones = []
    n = start
    while len(phones) < count:
        phones.append(str(n))
        n += 1
    return phones

def escape(s):
    if s is None:
        return "NULL"
    return "'" + str(s).replace("\\", "\\\\").replace("'", "\\'") + "'"

supplier_names = [
    "永辉供应链", "华润万家批发", "大润发配送", "盒马鲜生供应", "沃尔玛供应商"
]
short_names = ["永辉", "华润", "大润发", "盒马", "沃尔玛"]
addresses = [
    "北京市朝阳区建国路88号", "上海市浦东新区陆家嘴环路1000号",
    "广州市天河区体育西路111号", "深圳市南山区科技园路200号",
    "杭州市西湖区文三路300号", "成都市武侯区人民南路400号",
    "南京市鼓楼区中山北路500号", "武汉市江汉区解放大道600号"
]
contact_persons = [
    "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"
]

goods_names = [
    "可口可乐330ml", "百事可乐500ml", "农夫山泉550ml", "怡宝纯净水",
    "康师傅方便面", "统一老坛酸菜", "海飞丝洗发水", "潘婷护发素",
    "汰渍洗衣液", "蓝月亮洗手液", "维达纸巾", "清风面巾纸",
    "蒙牛纯牛奶", "伊利酸奶", "茅台酒500ml", "五粮液52度",
    "青岛啤酒330ml", "百威啤酒500ml", "雪花啤酒", "哈尔滨啤酒",
    "雀巢咖啡", "星巴克拿铁", "大白兔奶糖", "德芙巧克力",
    "奥利奥饼干", "康师傅冰红茶", "脉动维生素饮料", "红牛",
    "金龙鱼调和油", "鲁花花生油", "海天酱油", "老干妈辣椒酱",
    "太太乐鸡精", "王守义十三香", "三全水饺", "思念汤圆",
    "安井丸子", "双汇火腿肠", "金锣香肠", "雨润培根",
    "湾仔码头水饺", "光明鲜奶", "三元酸奶", "味全果汁",
    "汇源果汁", "椰树椰汁", "旺仔牛奶", "娃哈哈AD钙奶",
    "良品铺子坚果", "三只松鼠腰果", "百草味夏威夷果", "洽洽瓜子"
]

output_path = os.path.join(os.path.dirname(__file__), "test_data.sql")
phones = unique_phones(3000, start=13800000001)

with open(output_path, "w", encoding="utf-8") as f:
    f.write("USE supermarket_db;\n\n")

    # ======================== supplier (1000 rows) ========================
    f.write("-- 供应商 1000 条\n")
    f.write("INSERT INTO supplier (name, short_name, address, phone, email, contact_person, contact_phone, remark) VALUES\n")
    supplier_rows = []
    for i in range(1, 1001):
        name = supplier_names[i % len(supplier_names)] + f"({i})"
        short = short_names[i % len(short_names)]
        addr = addresses[i % len(addresses)]
        ph = phones[i - 1]  # use first 1000 phones
        email = f"supplier{i}@example.com"
        cp = contact_persons[i % len(contact_persons)]
        cph = str(13900000000 + i)
        remark = "无" if i % 3 != 0 else "VIP供应商"
        supplier_rows.append(
            f"({escape(name)}, {escape(short)}, {escape(addr)}, {escape(ph)}, {escape(email)}, {escape(cp)}, {escape(cph)}, {escape(remark)})"
        )
    f.write(",\n".join(supplier_rows) + ";\n\n")

    # ======================== goods (1000 rows) ========================
    f.write("-- 商品 1000 条\n")
    f.write("INSERT INTO goods (name, price, supplier_id, description, remark) VALUES\n")
    goods_rows = []
    for i in range(1, 1001):
        gname = f"商品_{i}" if i > len(goods_names) else goods_names[i - 1]
        if i <= len(goods_names):
            gname = goods_names[i - 1]
        else:
            gname = f"商品_{i}"
        price = round(random.uniform(1.0, 499.0), 2)
        sid = ((i - 1) % 1000) + 1  # distribute across suppliers
        desc = f"{gname}的详细描述"
        remark = "热销" if i % 5 == 0 else "常规商品"
        goods_rows.append(
            f"({escape(gname)}, {price}, {sid}, {escape(desc)}, {escape(remark)})"
        )
    f.write(",\n".join(goods_rows) + ";\n\n")

    # ======================== employee (1000 rows) ========================
    f.write("-- 员工 1000 条\n")
    f.write("INSERT INTO employee (name, password, level, phone, salary, remark, is_approved) VALUES\n")
    emp_names = [
        "张伟", "李娜", "王磊", "刘洋", "陈静", "杨帆", "赵敏", "黄强",
        "周杰", "吴婷", "徐亮", "孙悦", "马超", "朱红", "胡兵", "郭雪",
        "林枫", "何宇", "罗琳", "梁华", "宋涛", "郑娟", "谢军", "韩梅"
    ]
    emp_rows = []
    for i in range(1, 1001):
        ename = emp_names[i % len(emp_names)] + f"{i}"
        level = 2 if i <= 20 else 1  # first 20 are admins
        # employee phone uses phones[1000:] to avoid conflict with supplier
        eph = escape(phones[1000 + i - 1]) if i <= 900 else "NULL"
        salary = round(random.uniform(3000, 15000), 2)
        remark = "管理员" if level == 2 else "普通员工"
        is_approved = 1
        emp_rows.append(
            f"({escape(ename)}, {escape(PASSWORD_HASH)}, {level}, {eph}, {salary}, {escape(remark)}, {is_approved})"
        )
    f.write(",\n".join(emp_rows) + ";\n\n")

    # ======================== member (1000 rows) ========================
    f.write("-- 会员 1000 条\n")
    f.write("INSERT INTO member (name, phone, email, points, level, register_time, remark) VALUES\n")
    mem_names = [
        "陈晓", "王芳", "刘鹏", "赵丹", "李娟", "孙建", "周磊", "吴敏",
        "郑龙", "冯婷", "褚伟", "卫东", "蒋宁", "沈怡", "韩飞", "杨新"
    ]
    mem_rows = []
    level_dist = [1]*700 + [2]*200 + [3]*80 + [4]*20
    random.shuffle(level_dist)
    for i in range(1, 1001):
        mname = mem_names[i % len(mem_names)] + f"{i}"
        # member phones use phones[2000:] to avoid conflict
        mph = phones[2000 + i - 1]
        memail = f"member{i}@email.com" if i % 3 != 0 else "NULL"
        m_email = escape(memail)
        points = random.randint(0, 50000)
        mlevel = level_dist[i - 1]
        reg_time = f"2024-{random.randint(1,12):02d}-{random.randint(1,28):02d} {random.randint(0,23):02d}:{random.randint(0,59):02d}:{random.randint(0,59):02d}"
        remark = "优质会员" if mlevel >= 3 else "普通会员"
        mem_rows.append(
            f"({escape(mname)}, {escape(mph)}, {m_email}, {points}, {mlevel}, {escape(reg_time)}, {escape(remark)})"
        )
    f.write(",\n".join(mem_rows) + ";\n")

print(f"Generated: {output_path}")
print(f"  supplier: 1000 rows")
print(f"  goods: 1000 rows")
print(f"  employee: 1000 rows (900 with phone, 100 NULL phone)")
print(f"  member: 1000 rows")
