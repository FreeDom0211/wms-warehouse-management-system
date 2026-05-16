package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Permission;
import com.jd.wms.dao.entity.Role;
import com.jd.wms.dao.entity.RolePermission;
import com.jd.wms.dao.mapper.PermissionMapper;
import com.jd.wms.dao.mapper.RoleMapper;
import com.jd.wms.dao.mapper.RolePermissionMapper;
import com.jd.wms.service.PermissionService;
import com.jd.wms.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleService roleService;

    @Override
    public Permission getByPermCode(String permCode) {
        return baseMapper.selectByPermCode(permCode);
    }

    @Override
    @Transactional
    public boolean addPermission(Permission permission) {
        Permission existing = getByPermCode(permission.getPermCode());
        if (existing != null) {
            throw new WmsException("权限编码已存在");
        }
        permission.setCreateTime(new Date());
        permission.setUpdateTime(new Date());
        return save(permission);
    }

    @Override
    @Transactional
    public boolean updatePermission(Permission permission) {
        permission.setUpdateTime(new Date());
        return updateById(permission);
    }

    @Override
    @Transactional
    public boolean deletePermission(Long id) {
        Permission permission = getById(id);
        if (permission == null) {
            throw new WmsException("权限不存在");
        }
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("permission_id", id);
        rolePermissionMapper.delete(wrapper);
        return removeById(id);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(wrapper);

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> permIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
        return baseMapper.selectBatchIds(permIds);
    }

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        List<Role> userRoles = roleService.getRolesByUserId(userId);
        List<Permission> allPermissions = new ArrayList<>();
        for (Role role : userRoles) {
            List<Permission> rolePermissions = getPermissionsByRoleId(role.getId());
            allPermissions.addAll(rolePermissions);
        }
        return allPermissions;
    }

    @Override
    public List<Permission> getMenuPermissions(Long userId, String roleCode) {
        List<Permission> permissions = new ArrayList<>();

        if (roleCode != null) {
            Role role = roleMapper.selectByRoleCode(roleCode);
            if (role != null) {
                List<Permission> rolePerms = getPermissionsByRoleId(role.getId());
                permissions.addAll(rolePerms);
            }
        }

        List<Role> userRoles = roleService.getRolesByUserId(userId);
        for (Role role : userRoles) {
            List<Permission> rolePerms = getPermissionsByRoleId(role.getId());
            permissions.addAll(rolePerms);
        }

        return permissions.stream().distinct().collect(Collectors.toList());
    }

}