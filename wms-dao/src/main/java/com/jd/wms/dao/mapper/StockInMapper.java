package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.StockIn;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockInMapper extends BaseMapper<StockIn> {

    StockIn selectByStockInNo(String stockInNo);

}