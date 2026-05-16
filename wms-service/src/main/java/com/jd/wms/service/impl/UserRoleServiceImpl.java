package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.dao.entity.Role;
import com.jd.wms.dao.entity.UserRole;
import com.jd.wms.dao.mapper.UserRoleMapper;
import com.jd.wms.service.RoleService;
import com.jd.wms.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private RoleService roleService;

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserRole> userRoles = baseMapper.selectList(wrapper);
        return userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        baseMapper.delete(wrapper);

        List<UserRole> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreateTime(new Date());
            userRoles.add(ur);
        }
        return saveBatch(userRoles);
    }

    @Override
    @Transactional
    public boolean removeUserRoles(Long userId) {
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return baseMapper.delete(wrapper) >= 0;
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        List<Role> userRoles = roleService.getRolesByUserId(userId);
        return userRoles.stream().anyMatch(r -> r.getRoleCode().equals(roleCode));
    }

}