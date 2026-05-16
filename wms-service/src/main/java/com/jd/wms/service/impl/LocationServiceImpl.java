package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Inventory;
import com.jd.wms.dao.entity.Location;
import com.jd.wms.dao.mapper.InventoryMapper;
import com.jd.wms.dao.mapper.LocationMapper;
import com.jd.wms.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public Location getByLocationCode(String locationCode) {
        return baseMapper.selectByLocationCode(locationCode);
    }

    @Override
    @Transactional
    public boolean addLocation(Location location) {
        Location existing = getByLocationCode(location.getLocationCode());
        if (existing != null) {
            throw new WmsException("货位编码已存在");
        }
        location.setStatus("EMPTY");
        location.setCreateTime(new Date());
        location.setUpdateTime(new Date());
        return save(location);
    }

    @Override
    @Transactional
    public boolean updateLocation(Location location) {
        location.setUpdateTime(new Date());
        return updateById(location);
    }

    @Override
    @Transactional
    public boolean deleteLocation(Long id) {
        Location location = getById(id);
        if (location == null) {
            throw new WmsException("货位不存在");
        }
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean moveInventory(Long fromLocationId, Long toLocationId, Long productId,
                                 String batchNo, Integer quantity) {
        if (fromLocationId.equals(toLocationId)) {
            throw new WmsException("原货位和目标货位不能相同");
        }

        Location fromLocation = getById(fromLocationId);
        Location toLocation = getById(toLocationId);
        
        if (fromLocation == null) {
            throw new WmsException("原货位不存在");
        }
        if (toLocation == null) {
            throw new WmsException("目标货位不存在");
        }

        Inventory sourceInventory = inventoryMapper.selectOne(
            Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getLocationId, fromLocationId)
                .eq(Inventory::getProductId, productId)
                .eq(Inventory::getBatchNo, batchNo)
        );

        if (sourceInventory == null) {
            throw new WmsException("原货位不存在该商品批次");
        }
        if (sourceInventory.getQuantity() < quantity) {
            throw new WmsException("库存不足");
        }

        int updated = inventoryMapper.update(
            sourceInventory,
            Wrappers.<Inventory>lambdaUpdate()
                .eq(Inventory::getId, sourceInventory.getId())
                .eq(Inventory::getVersion, sourceInventory.getVersion())
        );
        if (updated == 0) {
            throw new WmsException("库存更新冲突，请重试");
        }

        sourceInventory.setQuantity(sourceInventory.getQuantity() - quantity);
        sourceInventory.setUpdateTime(new Date());
        inventoryMapper.updateById(sourceInventory);

        Inventory targetInventory = inventoryMapper.selectOne(
            Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getLocationId, toLocationId)
                .eq(Inventory::getProductId, productId)
                .eq(Inventory::getBatchNo, batchNo)
        );

        if (targetInventory != null) {
            targetInventory.setQuantity(targetInventory.getQuantity() + quantity);
            targetInventory.setUpdateTime(new Date());
            inventoryMapper.updateById(targetInventory);
        } else {
            targetInventory = new Inventory();
            targetInventory.setProductId(productId);
            targetInventory.setLocationId(toLocationId);
            targetInventory.setBatchNo(batchNo);
            targetInventory.setQuantity(quantity);
            targetInventory.setVersion(0);
            targetInventory.setCreateTime(new Date());
            targetInventory.setUpdateTime(new Date());
            inventoryMapper.insert(targetInventory);
        }

        if (sourceInventory.getQuantity() == 0) {
            fromLocation.setStatus("EMPTY");
            inventoryMapper.deleteById(sourceInventory.getId());
        }
        fromLocation.setUpdateTime(new Date());
        updateById(fromLocation);

        if (!"OCCUPIED".equals(toLocation.getStatus())) {
            toLocation.setStatus("OCCUPIED");
            toLocation.setUpdateTime(new Date());
            updateById(toLocation);
        }

        return true;
    }

    @Override
    public List<Map<String, Object>> getLocationInventory(Long locationId) {
        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        List<Inventory> inventories = inventoryMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Inventory inv : inventories) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", inv.getId());
            map.put("productId", inv.getProductId());
            map.put("batchNo", inv.getBatchNo());
            map.put("quantity", inv.getQuantity());
            result.add(map);
        }
        return result;
    }

}