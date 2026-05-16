package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.Location;

import java.util.List;
import java.util.Map;

public interface LocationService extends IService<Location> {

    Location getByLocationCode(String locationCode);

    boolean addLocation(Location location);

    boolean updateLocation(Location location);

    boolean deleteLocation(Long id);

    boolean moveInventory(Long fromLocationId, Long toLocationId, Long productId, 
                          String batchNo, Integer quantity);

    List<Map<String, Object>> getLocationInventory(Long locationId);

}