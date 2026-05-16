package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.dao.entity.RolePermission;
import com.jd.wms.dao.mapper.RolePermissionMapper;
import com.jd.wms.service.RolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        List<RolePermission> rolePermissions = baseMapper.selectList(wrapper);
        return rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        baseMapper.delete(wrapper);

        List<RolePermission> rolePermissions = new ArrayList<>();
        for (Long permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rp.setCreateTime(new Date());
            rolePermissions.add(rp);
        }
        return saveBatch(rolePermissions);
    }

    @Override
    @Transactional
    public boolean removeRolePermissions(Long roleId) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("role_id", roleId);
        return baseMapper.delete(wrapper) >= 0;
    }

}