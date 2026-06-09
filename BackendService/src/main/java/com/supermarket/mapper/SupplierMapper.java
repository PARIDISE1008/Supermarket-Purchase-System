package com.supermarket.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.supermarket.entity.Supplier;

@Mapper
public interface SupplierMapper {

    // 新增
    int insert(Supplier supplier);

    // 批量新增
    int batchInsert(@Param("list") List<Supplier> list);

    // 根据ID查询（查未删除的）
    Supplier selectById(@Param("id") Integer id);

    // 根据电话查询（查未删除的，用于查重）
    Supplier selectByPhone(@Param("phone") String phone);

    // 分页查询（按名称模糊搜索，只查未删除的）
    List<Supplier> selectPage(@Param("name") String name,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    // 统计总数
    int count(@Param("name") String name);

    // 修改
    int update(Supplier supplier);

    // 逻辑删除
    int deleteById(@Param("id") Integer id);

    // 查询关联的商品数量（用于删除前检查）
    int countGoodsBySupplierId(@Param("supplierId") Integer supplierId);

    // 批量查询（用于批量导入时查重，一次IN查询替代N次单查）
    List<Supplier> selectByPhones(@Param("phones") List<String> phones);
}
