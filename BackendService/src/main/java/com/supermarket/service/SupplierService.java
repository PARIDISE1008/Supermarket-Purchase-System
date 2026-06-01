package com.supermarket.service;

import com.supermarket.common.Result;
import com.supermarket.entity.Supplier;
import java.util.List;

public interface SupplierService {
    
    /** 新增供应商 */
    void add(Supplier supplier);
    
    /** 批量导入 */
    void batchImport(List<Supplier> suppliers);
    
    /** 分页查询 */
    Result<List<Supplier>> search(String name, Integer page, Integer size);
    
    /** 根据ID查询 */
    Supplier getById(Integer id);
    
    /** 修改 */
    void update(Supplier supplier);
    
    /** 删除（逻辑删除） */
    void delete(Integer id);
}