package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import com.supermarket.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Supplier supplier) {
        supplierService.add(supplier);
        return Result.success("新增供应商成功", null);
    }

    @PostMapping("/batch")
    public Result<Void> batchImport(@RequestBody List<Supplier> suppliers) {
        supplierService.batchImport(suppliers);
        return Result.success("批量导入成功", null);
    }

    @GetMapping
    public Result<List<Supplier>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return supplierService.search(name, page, size);
    }

    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable Integer id) {
        Supplier supplier = supplierService.getById(id);
        return Result.success(supplier);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Supplier supplier) {
        supplierService.update(supplier);
        return Result.success("修改供应商成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        supplierService.delete(id);
        return Result.success("删除供应商成功", null);
    }
}