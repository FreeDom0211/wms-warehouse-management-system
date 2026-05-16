package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByWorkNo(String workNo);

    User selectByPhone(String phone);

}