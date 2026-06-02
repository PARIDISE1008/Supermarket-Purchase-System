package com.supermarket.controller;

import java.util.List;

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
import com.supermarket.entity.Goods;
import com.supermarket.service.GoodsService;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Goods goods) {
        goodsService.add(goods);
        return Result.success("新增商品成功", null);
    }

    @PostMapping("/batch")
    public Result<Void> batchImport(@RequestBody List<Goods> goodsList) {
        goodsService.batchImport(goodsList);
        return Result.success("批量导入成功", null);
    }

    @GetMapping
    public Result<List<Goods>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return goodsService.search(name, supplierId, page, size);
    }

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Integer id) {
        Goods goods = goodsService.getById(id);
        return Result.success(goods);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Goods goods) {
        goodsService.update(goods);
        return Result.success("修改商品成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        goodsService.delete(id);
        return Result.success("删除商品成功", null);
    }
}
