package com.supermarket.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.common.Result;
import com.supermarket.entity.Employee;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.EmployeeMapper;
import com.supermarket.service.EmployeeService;
import com.supermarket.util.PasswordUtil;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public void register(Employee employee) {
        validateEmployee(employee);
        if (employee.getPhone() == null || employee.getPhone().isEmpty()) {
            log.warn("[注册失败] 手机号为空");
            throw BusinessException.paramError("手机号不能为空");
        }
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            log.warn("[注册失败] 密码为空");
            throw BusinessException.paramError("密码不能为空");
        }

        Employee existing = employeeMapper.selectByPhone(employee.getPhone());
        if (existing != null) {
            log.warn("[注册失败] 手机号已存在 phone={}", employee.getPhone());
            throw BusinessException.duplicate("手机号已注册");
        }

        employee.setPassword(PasswordUtil.encode(employee.getPassword()));
        employee.setLevel(1);
        employee.setIsApproved(0);
        int rows = employeeMapper.insert(employee);
        if (rows == 0) {
            log.error("[注册失败] 数据库插入返回0");
            throw BusinessException.operationFailed("注册失败");
        }

        log.info("[注册成功] id={}, phone={}, 等待审批", employee.getId(), employee.getPhone());
    }

    @Override
    public void add(Employee employee) {
        validateEmployee(employee);

        if (employee.getPhone() != null && !employee.getPhone().isEmpty()) {
            Employee existing = employeeMapper.selectByPhone(employee.getPhone());
            if (existing != null) {
                log.warn("[新增员工失败] 电话重复 phone={}", employee.getPhone());
                throw BusinessException.duplicate("员工电话");
            }
        }

        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            employee.setPassword("123456");
        }
        employee.setPassword(PasswordUtil.encode(employee.getPassword()));

        if (employee.getLevel() == null) {
            employee.setLevel(1);
        }
        employee.setIsApproved(1);

        int rows = employeeMapper.insert(employee);
        if (rows == 0) {
            log.error("[新增员工失败] 数据库插入返回0 name={}", employee.getName());
            throw BusinessException.operationFailed("新增员工失败");
        }

        log.info("[新增员工成功] id={}, name={}, level={}", employee.getId(), employee.getName(), employee.getLevel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            log.warn("[批量导入员工失败] 数据为空");
            throw BusinessException.paramError("导入数据不能为空");
        }

        log.info("[批量导入员工开始] 总数={}", employees.size());

        // 一遍循环完成：校验 + 设默认值
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            try {
                validateEmployee(emp);

                if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
                    emp.setPassword("123456");
                }
                emp.setPassword(PasswordUtil.encode(emp.getPassword()));
                if (emp.getLevel() == null) {
                    emp.setLevel(1);
                }
                emp.setIsApproved(1);
            } catch (BusinessException e) {
                errors.add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            log.warn("[批量导入员工失败] 校验不通过 errors={}", errors);
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }

        try {
            employeeMapper.batchInsert(employees);
            log.info("[批量导入员工成功] 共{}条", employees.size());
        } catch (Exception e) {
            log.error("[批量导入员工失败] 数据库异常，事务已回滚", e);
            throw new BusinessException("批量导入失败，数据已回滚");
        }
    }

    @Override
    public Result<List<Employee>> search(String name, Integer level, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        int offset = (page - 1) * size;
        List<Employee> list = employeeMapper.selectPage(name, level, offset, size);
        int total = employeeMapper.count(name, level);

        for (Employee emp : list) {
            emp.setPassword(null);
        }

        log.debug("[查询员工] name={}, level={}, page={}, total={}", name, level, page, total);
        return Result.success("查询成功", list, total);
    }

    @Override
    public Employee getById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[查询员工失败] 非法ID id={}", id);
            throw BusinessException.paramError("员工ID不合法");
        }

        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            log.warn("[查询员工失败] 不存在 id={}", id);
            throw BusinessException.notFound("员工");
        }

        employee.setPassword(null);
        return employee;
    }

    @Override
    public void update(Employee employee) {
        if (employee.getId() == null || employee.getId() <= 0) {
            log.warn("[修改员工失败] 非法ID id={}", employee.getId());
            throw BusinessException.paramError("员工ID不合法");
        }
        validateEmployee(employee);

        Employee existing = employeeMapper.selectById(employee.getId());
        if (existing == null) {
            log.warn("[修改员工失败] 不存在 id={}", employee.getId());
            throw BusinessException.notFound("员工");
        }

        if (employee.getPhone() != null && !employee.getPhone().isEmpty()) {
            Employee phoneExists = employeeMapper.selectByPhone(employee.getPhone());
            if (phoneExists != null && !phoneExists.getId().equals(employee.getId())) {
                log.warn("[修改员工失败] 电话重复 id={}, phone={}", employee.getId(), employee.getPhone());
                throw BusinessException.duplicate("员工电话");
            }
        }

        int rows = employeeMapper.update(employee);
        if (rows == 0) {
            log.error("[修改员工失败] 数据库更新返回0 id={}", employee.getId());
            throw BusinessException.operationFailed("修改员工失败");
        }

        log.info("[修改员工成功] id={}, name={}", employee.getId(), employee.getName());
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[删除员工失败] 非法ID id={}", id);
            throw BusinessException.paramError("员工ID不合法");
        }

        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            log.warn("[删除员工失败] 不存在 id={}", id);
            throw BusinessException.notFound("员工");
        }

        int purchaseCount = employeeMapper.countPurchaseByEmployeeId(id);
        if (purchaseCount > 0) {
            log.warn("[删除员工失败] 存在采购记录 id={}, purchaseCount={}", id, purchaseCount);
            throw new BusinessException(
                    String.format("该员工有%d条采购记录，无法删除", purchaseCount));
        }

        int rows = employeeMapper.deleteById(id);
        if (rows == 0) {
            log.error("[删除员工失败] 数据库更新返回0 id={}", id);
            throw BusinessException.operationFailed("删除员工失败");
        }

        log.info("[删除员工成功] id={}, name={}（已标记离职）", id, employee.getName());
    }

    @Override
    public Result<List<Employee>> getPending(Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int offset = (page - 1) * size;
        List<Employee> list = employeeMapper.selectPending(offset, size);
        int total = employeeMapper.countPending();
        for (Employee emp : list) {
            emp.setPassword(null);
        }
        return Result.success("查询成功", list, total);
    }

    @Override
    public void approve(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("员工ID不合法");
        }
        int rows = employeeMapper.approve(id);
        if (rows == 0) {
            throw BusinessException.notFound("员工");
        }
        log.info("[审批通过] employeeId={}", id);
    }

    @Override
    public void reject(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("员工ID不合法");
        }
        int rows = employeeMapper.reject(id);
        if (rows == 0) {
            throw BusinessException.notFound("员工");
        }
        log.info("[审批拒绝] employeeId={}", id);
    }

    @Override
    public Employee login(String phone, String password) {
        if (phone == null || phone.isEmpty()) {
            log.warn("[登录失败] 手机号为空");
            throw BusinessException.paramError("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            log.warn("[登录失败] 密码为空 phone={}", phone);
            throw BusinessException.paramError("密码不能为空");
        }

        Employee employee = employeeMapper.selectByPhone(phone);
        if (employee == null) {
            log.warn("[登录失败] 账号不存在 phone={}", phone);
            throw new BusinessException("账号不存在");
        }

        if (!PasswordUtil.matches(password, employee.getPassword())) {
            log.warn("[登录失败] 密码错误 phone={}", phone);
            throw new BusinessException("密码错误");
        }

        if (employee.getIsApproved() != null && employee.getIsApproved() == 0) {
            log.warn("[登录失败] 账号未审批 phone={}", phone);
            throw new BusinessException("您的账号正在等待管理员审批");
        }

        employee.setPassword(null);
        log.info("[登录成功] id={}, name={}", employee.getId(), employee.getName());
        return employee;
    }

    private void validateEmployee(Employee employee) {
        if (employee == null) {
            throw BusinessException.paramError("员工信息不能为空");
        }
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw BusinessException.paramError("员工姓名不能为空");
        }
        if (employee.getName().length() > 50) {
            throw BusinessException.paramError("员工姓名长度不能超过50");
        }
        if (employee.getPhone() != null && !employee.getPhone().isEmpty()
                && !employee.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw BusinessException.paramError("手机号格式不正确");
        }
        if (employee.getSalary() != null && employee.getSalary().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw BusinessException.paramError("工资不能为负数");
        }
    }
}
