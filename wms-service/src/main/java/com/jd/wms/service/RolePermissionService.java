package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.RolePermission;

import java.util.List;

public interface RolePermissionService extends IService<RolePermission> {

    List<Long> getPermissionIdsByRoleId(Long roleId);

    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    boolean removeRolePermissions(Long roleId);

}