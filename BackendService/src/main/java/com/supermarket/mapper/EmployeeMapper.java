package com.supermarket.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.supermarket.entity.Employee;

@Mapper
public interface EmployeeMapper {

    int insert(Employee employee);

    int batchInsert(@Param("list") List<Employee> list);

    Employee selectById(@Param("id") Integer id);

    Employee selectByPhone(@Param("phone") String phone);

    List<Employee> selectPage(@Param("name") String name,
                               @Param("level") Integer level,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    int count(@Param("name") String name,
              @Param("level") Integer level);

    int update(Employee employee);

    int deleteById(@Param("id") Integer id);

    int countPurchaseByEmployeeId(@Param("employeeId") Integer employeeId);

    List<Employee> selectPending(@Param("offset") Integer offset, @Param("limit") Integer limit);

    int countPending();

    int approve(@Param("id") Integer id);

    int reject(@Param("id") Integer id);
}
