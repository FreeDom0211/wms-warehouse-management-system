package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Inventory;
import com.jd.wms.dao.entity.Location;
import com.jd.wms.dao.entity.StockIn;
import com.jd.wms.dao.entity.Task;
import com.jd.wms.dao.mapper.InventoryMapper;
import com.jd.wms.dao.mapper.LocationMapper;
import com.jd.wms.dao.mapper.StockInMapper;
import com.jd.wms.service.DistributedLockService;
import com.jd.wms.service.StockInService;
import com.jd.wms.service.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StockInServiceImpl extends ServiceImpl<StockInMapper, StockIn> implements StockInService {

    private static final String INVENTORY_LOCK_PREFIX = "inventory:lock:";

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private DistributedLockService lockService;

    @Autowired(required = false)
    private WebSocketNotificationService notificationService;

    @Override
    public StockIn getByStockInNo(String stockInNo) {
        return baseMapper.selectByStockInNo(stockInNo);
    }

    @Override
    @Transactional
    public boolean addStockIn(StockIn stockIn) {
        String lockKey = INVENTORY_LOCK_PREFIX + stockIn.getProductId() + ":" + stockIn.getLocationId() + ":" + stockIn.getBatchNo();

        return lockService.executeWithLock(lockKey, 5, 30, () -> {
            stockIn.setStockInNo("IN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            stockIn.setStatus("PENDING");
            stockIn.setCreateTime(new Date());
            stockIn.setUpdateTime(new Date());

            boolean result = save(stockIn);

            Inventory inventory = inventoryMapper.selectOne(
                Wrappers.<Inventory>lambdaQuery()
                    .eq(Inventory::getProductId, stockIn.getProductId())
                    .eq(Inventory::getLocationId, stockIn.getLocationId())
                    .eq(Inventory::getBatchNo, stockIn.getBatchNo())
            );

            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + stockIn.getQuantity());
                inventory.setUpdateTime(new Date());
                inventoryMapper.updateById(inventory);
            } else {
                inventory = new Inventory();
                inventory.setProductId(stockIn.getProductId());
                inventory.setLocationId(stockIn.getLocationId());
                inventory.setBatchNo(stockIn.getBatchNo());
                inventory.setQuantity(stockIn.getQuantity());
                inventory.setVersion(0);
                inventory.setCreateTime(new Date());
                inventory.setUpdateTime(new Date());
                inventoryMapper.insert(inventory);
            }

            Location location = locationMapper.selectById(stockIn.getLocationId());
            if (location != null) {
                location.setStatus("OCCUPIED");
                location.setUpdateTime(new Date());
                locationMapper.updateById(location);
            }

            stockIn.setStatus("COMPLETED");
            updateById(stockIn);

            return result;
        });
    }

    @Override
    @Transactional
    public boolean updateStockIn(StockIn stockIn) {
        stockIn.setUpdateTime(new Date());
        return updateById(stockIn);
    }

    @Override
    @Transactional
    public boolean deleteStockIn(Long id) {
        StockIn stockIn = getById(id);
        if (stockIn == null) {
            throw new WmsException("入库记录不存在");
        }
        return removeById(id);
    }

    @Override
    public List<StockIn> getPendingTasks(Long operatorId) {
        QueryWrapper<StockIn> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId).eq("status", "PENDING").orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Map<String, Object> createStockInTask(Long operatorId, Long productId, String batchNo,
                                                  Long locationId, Integer quantity, String remark) {
        StockIn stockIn = new StockIn();
        stockIn.setStockInNo("IN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        stockIn.setOperatorId(operatorId);
        stockIn.setProductId(productId);
        stockIn.setBatchNo(batchNo);
        stockIn.setLocationId(locationId);
        stockIn.setQuantity(quantity);
        stockIn.setRemark(remark);
        stockIn.setStatus("PENDING");
        stockIn.setCreateTime(new Date());
        stockIn.setUpdateTime(new Date());

        save(stockIn);

        if (notificationService != null) {
            Task task = convertStockInToTask(stockIn);
            notificationService.notifyTaskAssigned(operatorId, task);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stockIn", stockIn);
        result.put("message", "入库任务创建成功");

        return result;
    }

    @Override
    @Transactional
    public boolean completeStockIn(Long id) {
        StockIn stockIn = getById(id);
        if (stockIn == null) {
            throw new WmsException("入库任务不存在");
        }
        if (!"PENDING".equals(stockIn.getStatus())) {
            throw new WmsException("任务状态不是待入库");
        }

        String lockKey = INVENTORY_LOCK_PREFIX + stockIn.getProductId() + ":" + stockIn.getLocationId() + ":" + stockIn.getBatchNo();

        return lockService.executeWithLock(lockKey, 5, 30, () -> {
            Inventory inventory = inventoryMapper.selectOne(
                Wrappers.<Inventory>lambdaQuery()
                    .eq(Inventory::getProductId, stockIn.getProductId())
                    .eq(Inventory::getLocationId, stockIn.getLocationId())
                    .eq(Inventory::getBatchNo, stockIn.getBatchNo())
            );

            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + stockIn.getQuantity());
                inventory.setUpdateTime(new Date());
                inventoryMapper.updateById(inventory);
            } else {
                inventory = new Inventory();
                inventory.setProductId(stockIn.getProductId());
                inventory.setLocationId(stockIn.getLocationId());
                inventory.setBatchNo(stockIn.getBatchNo());
                inventory.setQuantity(stockIn.getQuantity());
                inventory.setVersion(0);
                inventory.setCreateTime(new Date());
                inventory.setUpdateTime(new Date());
                inventoryMapper.insert(inventory);
            }

            Location location = locationMapper.selectById(stockIn.getLocationId());
            if (location != null) {
                location.setStatus("OCCUPIED");
                location.setUpdateTime(new Date());
                locationMapper.updateById(location);
            }

            stockIn.setStatus("COMPLETED");
            stockIn.setUpdateTime(new Date());
            updateById(stockIn);

            if (notificationService != null) {
                Task task = convertStockInToTask(stockIn);
                notificationService.notifyTaskUpdate(task);
            }

            return true;
        });
    }

    @Override
    public List<Map<String, Object>> getFreeLocations(Long warehouseId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId).eq("status", "EMPTY");
        List<Location> locations = locationMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Location loc : locations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", loc.getId());
            map.put("locationCode", loc.getLocationCode());
            map.put("zone", loc.getZone());
            result.add(map);
        }
        return result;
    }

    private Task convertStockInToTask(StockIn stockIn) {
        Task task = new Task();
        task.setTaskType("STOCK_IN");
        task.setRelatedNo(stockIn.getStockInNo());
        task.setOperatorId(stockIn.getOperatorId());
        task.setStatus(stockIn.getStatus());
        task.setCreateTime(stockIn.getCreateTime());
        task.setUpdateTime(stockIn.getUpdateTime());
        return task;
    }

}