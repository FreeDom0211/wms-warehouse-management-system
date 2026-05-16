package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    Role getByRoleCode(String roleCode);

    boolean addRole(Role role);

    boolean updateRole(Role role);

    boolean deleteRole(Long id);

    List<Role> getRolesByUserId(Long userId);

    List<Role> getAllActiveRoles();

}