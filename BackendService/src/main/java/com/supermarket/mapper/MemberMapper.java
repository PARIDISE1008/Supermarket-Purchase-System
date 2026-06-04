package com.supermarket.mapper;

import com.supermarket.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {

    int insert(Member member);

    int batchInsert(@Param("list") List<Member> list);

    Member selectById(@Param("id") Integer id);

    Member selectByPhone(@Param("phone") String phone);

    List<Member> selectPage(@Param("name") String name,
            @Param("level") Integer level,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int count(@Param("name") String name,
            @Param("level") Integer level);

    int update(Member member);

    int deleteById(@Param("id") Integer id);
}
