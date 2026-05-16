package com.jd.wms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.wms.dao.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    Inventory selectByProductAndLocation(Long productId, Long locationId);

    List<Map<String, Object>> selectInventoryByWarehouse();

    List<Map<String, Object>> selectInventoryDetails(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId);

    List<Map<String, Object>> selectTopProducts(@Param("limit") int limit);

    List<Map<String, Object>> selectLowStockProducts(@Param("threshold") int threshold);

}