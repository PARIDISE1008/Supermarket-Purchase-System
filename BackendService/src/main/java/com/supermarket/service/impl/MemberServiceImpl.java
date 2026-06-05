package com.supermarket.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.common.Result;
import com.supermarket.entity.Member;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.MemberMapper;
import com.supermarket.service.MemberService;

@Service
public class MemberServiceImpl implements MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public void add(Member member) {
        validateMember(member);

        Member existing = memberMapper.selectByPhone(member.getPhone());
        if (existing != null) {
            log.warn("[新增会员失败] 电话重复 phone={}", member.getPhone());
            throw BusinessException.duplicate("会员电话");
        }

        if (member.getPoints() == null) {
            member.setPoints(0);
        }
        if (member.getLevel() == null) {
            member.setLevel(1);
        }
        if (member.getRegisterTime() == null) {
            member.setRegisterTime(LocalDateTime.now());
        }

        int rows = memberMapper.insert(member);
        if (rows == 0) {
            log.error("[新增会员失败] 数据库插入返回0 name={}", member.getName());
            throw BusinessException.operationFailed("新增会员失败");
        }

        log.info("[新增会员成功] id={}, name={}, level={}", member.getId(), member.getName(), member.getLevel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Member> members) {
        if (members == null || members.isEmpty()) {
            log.warn("[批量导入会员失败] 数据为空");
            throw BusinessException.paramError("导入数据不能为空");
        }

        log.info("[批量导入会员开始] 总数={}", members.size());

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            try {
                validateMember(members.get(i));
            } catch (BusinessException e) {
                errors.add(String.format("第%d行: %s", i + 1, e.getMessage()));
            }
        }

        if (!errors.isEmpty()) {
            log.warn("[批量导入会员失败] 校验不通过 errors={}", errors);
            throw BusinessException.paramError("数据校验失败：" + String.join("; ", errors));
        }

        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {
                if (members.get(i).getPhone().equals(members.get(j).getPhone())) {
                    log.warn("[批量导入会员失败] 内部电话重复 行{}和行{}", i + 1, j + 1);
                    throw BusinessException.paramError(
                            String.format("第%d行和第%d行电话重复", i + 1, j + 1));
                }
            }
        }

        for (Member member : members) {
            Member existing = memberMapper.selectByPhone(member.getPhone());
            if (existing != null) {
                log.warn("[批量导入会员失败] 电话已存在 phone={}", member.getPhone());
                throw BusinessException.duplicate("电话 " + member.getPhone());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (Member member : members) {
            if (member.getPoints() == null) {
                member.setPoints(0);
            }
            if (member.getLevel() == null) {
                member.setLevel(1);
            }
            if (member.getRegisterTime() == null) {
                member.setRegisterTime(now);
            }
        }

        try {
            memberMapper.batchInsert(members);
            log.info("[批量导入会员成功] 共{}条", members.size());
        } catch (Exception e) {
            log.error("[批量导入会员失败] 数据库异常，事务已回滚", e);
            throw new BusinessException("批量导入失败，数据已回滚");
        }
    }

    @Override
    public Result<List<Member>> search(String name, Integer level, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        int offset = (page - 1) * size;
        List<Member> list = memberMapper.selectPage(name, level, offset, size);
        int total = memberMapper.count(name, level);

        log.debug("[查询会员] name={}, level={}, page={}, total={}", name, level, page, total);
        return Result.success("查询成功", list, total);
    }

    @Override
    public Member getById(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[查询会员失败] 非法ID id={}", id);
            throw BusinessException.paramError("会员ID不合法");
        }

        Member member = memberMapper.selectById(id);
        if (member == null) {
            log.warn("[查询会员失败] 不存在 id={}", id);
            throw BusinessException.notFound("会员");
        }

        return member;
    }

    @Override
    public void update(Member member) {
        if (member.getId() == null || member.getId() <= 0) {
            log.warn("[修改会员失败] 非法ID id={}", member.getId());
            throw BusinessException.paramError("会员ID不合法");
        }
        validateMember(member);

        Member existing = memberMapper.selectById(member.getId());
        if (existing == null) {
            log.warn("[修改会员失败] 不存在 id={}", member.getId());
            throw BusinessException.notFound("会员");
        }

        Member phoneExists = memberMapper.selectByPhone(member.getPhone());
        if (phoneExists != null && !phoneExists.getId().equals(member.getId())) {
            log.warn("[修改会员失败] 电话重复 id={}, phone={}", member.getId(), member.getPhone());
            throw BusinessException.duplicate("会员电话");
        }

        int rows = memberMapper.update(member);
        if (rows == 0) {
            log.error("[修改会员失败] 数据库更新返回0 id={}", member.getId());
            throw BusinessException.operationFailed("修改会员失败");
        }

        log.info("[修改会员成功] id={}, name={}", member.getId(), member.getName());
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) {
            log.warn("[删除会员失败] 非法ID id={}", id);
            throw BusinessException.paramError("会员ID不合法");
        }

        Member member = memberMapper.selectById(id);
        if (member == null) {
            log.warn("[删除会员失败] 不存在 id={}", id);
            throw BusinessException.notFound("会员");
        }

        int rows = memberMapper.deleteById(id);
        if (rows == 0) {
            log.error("[删除会员失败] 数据库更新返回0 id={}", id);
            throw BusinessException.operationFailed("删除会员失败");
        }

        log.info("[删除会员成功] id={}, name={}（已标记注销）", id, member.getName());
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw BusinessException.paramError("会员信息不能为空");
        }
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw BusinessException.paramError("会员姓名不能为空");
        }
        if (member.getName().length() > 50) {
            throw BusinessException.paramError("会员姓名长度不能超过50");
        }
        if (member.getPhone() == null || member.getPhone().trim().isEmpty()) {
            throw BusinessException.paramError("会员电话不能为空");
        }
        if (!member.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw BusinessException.paramError("手机号格式不正确（11位手机号）");
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty()
                && !member.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw BusinessException.paramError("邮箱格式不正确");
        }
        if (member.getPoints() != null && member.getPoints() < 0) {
            throw BusinessException.paramError("积分不能为负数");
        }
        if (member.getLevel() != null && (member.getLevel() < 1 || member.getLevel() > 4)) {
            throw BusinessException.paramError("会员等级必须在1-4之间");
        }
    }
}
