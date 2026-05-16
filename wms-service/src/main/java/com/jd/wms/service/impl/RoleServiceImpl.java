package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Role;
import com.jd.wms.dao.entity.UserRole;
import com.jd.wms.dao.mapper.RoleMapper;
import com.jd.wms.dao.mapper.UserRoleMapper;
import com.jd.wms.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public Role getByRoleCode(String roleCode) {
        return baseMapper.selectByRoleCode(roleCode);
    }

    @Override
    @Transactional
    public boolean addRole(Role role) {
        Role existing = getByRoleCode(role.getRoleCode());
        if (existing != null) {
            throw new WmsException("角色编码已存在");
        }
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        return save(role);
    }

    @Override
    @Transactional
    public boolean updateRole(Role role) {
        role.setUpdateTime(new Date());
        return updateById(role);
    }

    @Override
    @Transactional
    public boolean deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new WmsException("角色不存在");
        }
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", id);
        userRoleMapper.delete(wrapper);
        return removeById(id);
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return baseMapper.selectBatchIds(roleIds);
    }

    @Override
    public List<Role> getAllActiveRoles() {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        return baseMapper.selectList(wrapper);
    }

}