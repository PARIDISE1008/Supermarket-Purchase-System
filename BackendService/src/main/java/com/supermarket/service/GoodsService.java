package com.supermarket.service;

import java.util.List;

import com.supermarket.common.Result;
import com.supermarket.entity.Goods;

public interface GoodsService {

    void add(Goods goods);

    void batchImport(List<Goods> goodsList);

    Result<List<Goods>> search(String name, Integer supplierId, Integer page, Integer size);

    Goods getById(Integer id);

    void update(Goods goods);

    void delete(Integer id);
}
