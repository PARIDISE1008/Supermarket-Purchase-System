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
import com.supermarket.entity.Member;
import com.supermarket.service.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Member member) {
        memberService.add(member);
        return Result.success("新增会员成功", null);
    }

    @PostMapping("/batch")
    public Result<Void> batchImport(@RequestBody List<Member> members) {
        memberService.batchImport(members);
        return Result.success("批量导入成功", null);
    }

    @GetMapping
    public Result<List<Member>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return memberService.search(name, level, page, size);
    }

    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Integer id) {
        Member member = memberService.getById(id);
        return Result.success(member);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Member member) {
        memberService.update(member);
        return Result.success("修改会员成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        memberService.delete(id);
        return Result.success("删除会员成功", null);
    }
}