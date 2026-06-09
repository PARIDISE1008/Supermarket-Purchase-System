package com.supermarket.service;

import java.util.List;

import com.supermarket.common.Result;
import com.supermarket.entity.Employee;

public interface EmployeeService {

    void add(Employee employee);

    void batchImport(List<Employee> employees);

    Result<List<Employee>> search(String name, Integer level, Integer page, Integer size);

    Employee getById(Integer id);

    void update(Employee employee);

    void delete(Integer id);

    /** 登录（根据手机号+密码验证） */
    Employee login(String phone, String password);

    /** 用户自行注册（is_approved=0，待审批） */
    void register(Employee employee);

    /** 查询待审批员工列表 */
    Result<List<Employee>> getPending(Integer page, Integer size);

    /** 通过审批 */
    void approve(Integer id);

    /** 拒绝注册 */
    void reject(Integer id);
}
