package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    Permission selectByPermCode(String permCode);

}