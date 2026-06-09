package com.supermarket.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supermarket.common.Result;
import com.supermarket.entity.Employee;
import com.supermarket.service.EmployeeService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /** 新增员工 */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Employee employee) {
        employeeService.add(employee);
        return Result.success("新增员工成功", null);
    }

    /** 批量导入 */
    @PostMapping("/batch")
    public Result<Void> batchImport(@RequestBody List<Employee> employees) {
        employeeService.batchImport(employees);
        return Result.success("批量导入成功", null);
    }

    /** 分页查询 */
    @GetMapping
    public Result<List<Employee>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return employeeService.search(name, level, page, size);
    }

    /** 查单个 */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Integer id) {
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /** 修改 */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody Employee employee) {
        employeeService.update(employee);
        return Result.success("修改员工成功", null);
    }

    /** 删除（标记离职） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        employeeService.delete(id);
        return Result.success("删除员工成功", null);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<Employee> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        Employee employee = employeeService.login(phone, password);
        return Result.success("登录成功", employee);
    }

    /** 用户自行注册 */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody Employee employee) {
        employeeService.register(employee);
        return Result.success("注册成功，等待管理员审批", null);
    }

    /** 查询待审批员工 */
    @GetMapping("/pending")
    public Result<List<Employee>> getPending(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return employeeService.getPending(page, size);
    }

    /** 通过审批 */
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Integer id) {
        employeeService.approve(id);
        return Result.success("审批通过", null);
    }

    /** 拒绝 */
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Integer id) {
        employeeService.reject(id);
        return Result.success("已拒绝", null);
    }
}
