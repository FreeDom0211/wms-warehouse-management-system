package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.InventoryCheck;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck> {

    InventoryCheck selectByCheckNo(String checkNo);

}