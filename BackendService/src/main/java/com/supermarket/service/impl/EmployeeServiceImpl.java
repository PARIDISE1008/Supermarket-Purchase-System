package com.supermarket.service.impl;

import java.util.ArrayList;
import java.util.List;

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

    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public void add(Employee employee) {
        validateEmployee(employee);

        // 电话查重
        if (employee.getPhone() != null && !employee.getPhone().isEmpty()) {
            Employee existing = employeeMapper.selectByPhone(employee.getPhone());
            if (existing != null) {
                throw BusinessException.duplicate("员工电话");
            }
        }

        // 密码默认 123456，加密存储
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            employee.setPassword("123456");
        }
        employee.setPassword(PasswordUtil.encode(employee.getPassword()));

        // 级别默认 1
        if (employee.getLevel() == null) {
            employee.setLevel(1);
        }

        int rows = employeeMapper.insert(employee);
        if (rows == 0) {
            throw BusinessException.operationFailed("新增员工失败");
        }

        System.out.println("新增员工成功，ID=" + employee.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            throw BusinessException.paramError("导入数据不能为空");
        }

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < employees.size(); i++) {
            try {
                validateEmployee(employees.get(i));
            } catch (BusinessException e) {
                errors.add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }

        // 统一加密密码
        for (Employee emp : employees) {
            if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
                emp.setPassword("123456");
            }
            emp.setPassword(PasswordUtil.encode(emp.getPassword()));
            if (emp.getLevel() == null) {
                emp.setLevel(1);
            }
        }

        employeeMapper.batchInsert(employees);
        System.out.println("批量导入员工成功，共" + employees.size() + "条");
    }

    @Override
    public Result<List<Employee>> search(String name, Integer level, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        int offset = (page - 1) * size;
        List<Employee> list = employeeMapper.selectPage(name, level, offset, size);
        int total = employeeMapper.count(name, level);

        // 返回前清空密码字段，安全考虑
        for (Employee emp : list) {
            emp.setPassword(null);
        }

        return Result.success("查询成功", list, total);
    }

    @Override
    public Employee getById(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("员工ID不合法");
        }

        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw BusinessException.notFound("员工");
        }

        // 不返回密码
        employee.setPassword(null);
        return employee;
    }

    @Override
    public void update(Employee employee) {
        if (employee.getId() == null || employee.getId() <= 0) {
            throw BusinessException.paramError("员工ID不合法");
        }
        validateEmployee(employee);

        Employee existing = employeeMapper.selectById(employee.getId());
        if (existing == null) {
            throw BusinessException.notFound("员工");
        }

        // 电话查重（排除自己）
        if (employee.getPhone() != null && !employee.getPhone().isEmpty()) {
            Employee phoneExists = employeeMapper.selectByPhone(employee.getPhone());
            if (phoneExists != null && !phoneExists.getId().equals(employee.getId())) {
                throw BusinessException.duplicate("员工电话");
            }
        }

        int rows = employeeMapper.update(employee);
        if (rows == 0) {
            throw BusinessException.operationFailed("修改员工失败");
        }

        System.out.println("修改员工成功，ID=" + employee.getId());
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            throw BusinessException.paramError("员工ID不合法");
        }

        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw BusinessException.notFound("员工");
        }

        // 检查是否有采购记录
        int purchaseCount = employeeMapper.countPurchaseByEmployeeId(id);
        if (purchaseCount > 0) {
            throw new BusinessException(
                String.format("该员工有%d条采购记录，无法删除（可标记为离职）", purchaseCount));
        }

        int rows = employeeMapper.deleteById(id);
        if (rows == 0) {
            throw BusinessException.operationFailed("删除员工失败");
        }

        System.out.println("删除员工成功（已标记离职），ID=" + id);
    }

    @Override
    public Employee login(String phone, String password) {
        if (phone == null || phone.isEmpty()) {
            throw BusinessException.paramError("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw BusinessException.paramError("密码不能为空");
        }

        Employee employee = employeeMapper.selectByPhone(phone);
        if (employee == null) {
            throw new BusinessException("账号不存在");
        }

        if (!PasswordUtil.matches(password, employee.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 不返回密码
        employee.setPassword(null);
        System.out.println("员工登录成功：" + employee.getName());
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
