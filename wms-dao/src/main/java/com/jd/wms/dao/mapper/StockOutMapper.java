package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.StockOut;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockOutMapper extends BaseMapper<StockOut> {

    StockOut selectByStockOutNo(String stockOutNo);

}