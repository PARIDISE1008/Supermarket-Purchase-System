package com.supermarket.service;

import java.util.List;

import com.supermarket.common.Result;
import com.supermarket.entity.Member;

public interface MemberService {

    void add(Member member);

    void batchImport(List<Member> members);

    Result<List<Member>> search(String name, Integer level, Integer page, Integer size);

    Member getById(Integer id);

    void update(Member member);

    void delete(Integer id);

}
