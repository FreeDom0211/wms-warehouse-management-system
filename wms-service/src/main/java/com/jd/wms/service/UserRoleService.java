package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.UserRole;

import java.util.List;

public interface UserRoleService extends IService<UserRole> {

    List<Long> getRoleIdsByUserId(Long userId);

    boolean assignRoles(Long userId, List<Long> roleIds);

    boolean removeUserRoles(Long userId);

    boolean hasRole(Long userId, String roleCode);

}