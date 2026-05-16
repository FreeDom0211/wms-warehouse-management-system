package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Location;
import com.jd.wms.dao.entity.Warehouse;
import com.jd.wms.dao.mapper.LocationMapper;
import com.jd.wms.dao.mapper.WarehouseMapper;
import com.jd.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public Warehouse getByName(String name) {
        return baseMapper.selectByName(name);
    }

    @Override
    @Transactional
    public boolean addWarehouse(Warehouse warehouse) {
        if (warehouse.getWarehouseName() != null && !warehouse.getWarehouseName().isEmpty()) {
            warehouse.setName(warehouse.getWarehouseName());
        }
        Warehouse existing = getByName(warehouse.getName());
        if (existing != null) {
            throw new WmsException("仓库名称已存在");
        }
        warehouse.setStatus(1);
        warehouse.setCreateTime(new Date());
        warehouse.setUpdateTime(new Date());
        return save(warehouse);
    }

    @Override
    @Transactional
    public boolean updateWarehouse(Warehouse warehouse) {
        warehouse.setUpdateTime(new Date());
        return updateById(warehouse);
    }

    @Override
    @Transactional
    public boolean deleteWarehouse(Long id) {
        Warehouse warehouse = getById(id);
        if (warehouse == null) {
            throw new WmsException("仓库不存在");
        }

        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", id);
        locationMapper.delete(wrapper);

        return removeById(id);
    }

    @Override
    @Transactional
    public boolean updateWarehouseId(Long oldId, Long newId) {
        Warehouse existing = getById(newId);
        if (existing != null) {
            throw new WmsException("新ID已被占用");
        }
        
        UpdateWrapper<Location> locationUpdateWrapper = new UpdateWrapper<>();
        locationUpdateWrapper.eq("warehouse_id", oldId).set("warehouse_id", newId);
        locationMapper.update(null, locationUpdateWrapper);
        
        warehouseMapper.updateWarehouseId(oldId, newId);
        
        return true;
    }

    @Override
    @Transactional
    public boolean batchGenerateLocations(Long warehouseId, String zoneInfo, String rulePattern) {
        Warehouse warehouse = getById(warehouseId);
        if (warehouse == null) {
            throw new WmsException("仓库不存在");
        }

        if (zoneInfo != null && !zoneInfo.isEmpty()) {
            String[] zones = zoneInfo.split(",");
            for (String zone : zones) {
                String[] parts = zone.split(":");
                if (parts.length == 2) {
                    String zoneCode = parts[0].trim();
                    generateLocationsForZone(warehouseId, zoneCode, rulePattern);
                }
            }
        }

        warehouse.setZoneInfo(zoneInfo);
        warehouse.setUpdateTime(new Date());
        updateById(warehouse);

        return true;
    }

    private void generateLocationsForZone(Long warehouseId, String zoneCode, String rulePattern) {
        String[] patternParts = rulePattern.split("-");
        if (patternParts.length != 4) {
            throw new WmsException("货位编码规则格式错误，应为：区域-排-列-层");
        }

        int rows = Integer.parseInt(patternParts[1]);
        int cols = Integer.parseInt(patternParts[2]);
        int levels = Integer.parseInt(patternParts[3]);

        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                for (int level = 1; level <= levels; level++) {
                    String locationCode = String.format("%s%02d%02d%02d", zoneCode, row, col, level);

                    QueryWrapper<Location> checkWrapper = new QueryWrapper<>();
                    checkWrapper.eq("location_code", locationCode);
                    if (locationMapper.selectCount(checkWrapper) > 0) {
                        continue;
                    }

                    Location location = new Location();
                    location.setLocationCode(locationCode);
                    location.setWarehouseId(warehouseId);
                    location.setZone(zoneCode);
                    location.setStatus("EMPTY");
                    location.setCreateTime(new Date());
                    location.setUpdateTime(new Date());
                    locationMapper.insert(location);
                }
            }
        }
    }

}