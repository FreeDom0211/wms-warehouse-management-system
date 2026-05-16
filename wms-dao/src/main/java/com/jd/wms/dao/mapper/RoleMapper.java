package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    Role selectByRoleCode(String roleCode);

}