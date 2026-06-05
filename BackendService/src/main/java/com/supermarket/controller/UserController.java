// ============================================
// 路径: com/supermarket/controller/UserController.java
// ============================================
package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Employee;
import com.supermarket.entity.Goods;
import com.supermarket.service.EmployeeService;
import com.supermarket.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 普通用户控制器 登录后的员工可以查看自己的信息和公共数据
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 查看自己的员工信息 GET /api/user/info?employeeId=3
     */
    @GetMapping("/info")
    public Result<Employee> getMyInfo(@RequestParam Integer employeeId) {
        Employee employee = employeeService.getById(employeeId);
        return Result.success(employee);
    }

    /**
     * 查询所有商品（只读） GET /api/user/goods?name=手机&page=1
     */
    @GetMapping("/goods")
    public Result<List<Goods>> queryGoods(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return goodsService.search(name, null, page, size);
    }

    /**
     * 查看单个商品详情 GET /api/user/goods/1
     */
    @GetMapping("/goods/{id}")
    public Result<Goods> getGoodsDetail(@PathVariable Integer id) {
        Goods goods = goodsService.getById(id);
        return Result.success(goods);
    }
}
