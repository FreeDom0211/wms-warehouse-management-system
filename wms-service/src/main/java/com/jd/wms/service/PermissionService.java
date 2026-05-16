package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    Permission getByPermCode(String permCode);

    boolean addPermission(Permission permission);

    boolean updatePermission(Permission permission);

    boolean deletePermission(Long id);

    List<Permission> getPermissionsByRoleId(Long roleId);

    List<Permission> getPermissionsByUserId(Long userId);

    List<Permission> getMenuPermissions(Long userId, String roleCode);

}