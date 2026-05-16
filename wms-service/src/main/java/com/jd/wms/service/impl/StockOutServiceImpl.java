package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Inventory;
import com.jd.wms.dao.entity.Location;
import com.jd.wms.dao.entity.Product;
import com.jd.wms.dao.entity.StockOut;
import com.jd.wms.dao.entity.Task;
import com.jd.wms.dao.mapper.InventoryMapper;
import com.jd.wms.dao.mapper.LocationMapper;
import com.jd.wms.dao.mapper.ProductMapper;
import com.jd.wms.dao.mapper.StockOutMapper;
import com.jd.wms.service.DistributedLockService;
import com.jd.wms.service.StockOutService;
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
public class StockOutServiceImpl extends ServiceImpl<StockOutMapper, StockOut> implements StockOutService {

    private static final String INVENTORY_LOCK_PREFIX = "inventory:lock:";

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private DistributedLockService lockService;

    @Autowired(required = false)
    private WebSocketNotificationService notificationService;

    @Override
    public StockOut getByStockOutNo(String stockOutNo) {
        return baseMapper.selectByStockOutNo(stockOutNo);
    }

    @Override
    @Transactional
    public boolean addStockOut(StockOut stockOut) {
        String lockKey = INVENTORY_LOCK_PREFIX + stockOut.getProductId() + ":" + stockOut.getLocationId() + ":" + stockOut.getBatchNo();

        return lockService.executeWithLock(lockKey, 5, 30, () -> {
            Inventory inventory = inventoryMapper.selectOne(
                Wrappers.<Inventory>lambdaQuery()
                    .eq(Inventory::getProductId, stockOut.getProductId())
                    .eq(Inventory::getLocationId, stockOut.getLocationId())
                    .eq(Inventory::getBatchNo, stockOut.getBatchNo())
            );

            if (inventory == null || inventory.getQuantity() < stockOut.getQuantity()) {
                throw new WmsException("库存不足");
            }

            stockOut.setStockOutNo("OUT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            stockOut.setStatus("PENDING");
            stockOut.setCreateTime(new Date());
            stockOut.setUpdateTime(new Date());

            boolean result = save(stockOut);

            inventory.setQuantity(inventory.getQuantity() - stockOut.getQuantity());
            inventory.setUpdateTime(new Date());
            inventoryMapper.updateById(inventory);

            if (inventory.getQuantity() == 0) {
                Location location = locationMapper.selectById(stockOut.getLocationId());
                if (location != null) {
                    location.setStatus("EMPTY");
                    location.setUpdateTime(new Date());
                    locationMapper.updateById(location);
                }
                inventoryMapper.deleteById(inventory.getId());
            }

            stockOut.setStatus("COMPLETED");
            updateById(stockOut);

            return result;
        });
    }

    @Override
    @Transactional
    public boolean updateStockOut(StockOut stockOut) {
        stockOut.setUpdateTime(new Date());
        return updateById(stockOut);
    }

    @Override
    @Transactional
    public boolean deleteStockOut(Long id) {
        StockOut stockOut = getById(id);
        if (stockOut == null) {
            throw new WmsException("出库记录不存在");
        }
        return removeById(id);
    }

    @Override
    public List<StockOut> getPendingTasks(Long operatorId) {
        QueryWrapper<StockOut> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId).eq("status", "PENDING").orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> simulateOrderDetail(String orderNo) {
        Map<String, Object> order = new HashMap<>();
        order.put("orderNo", orderNo);
        order.put("createTime", new Date().toString());

        List<Map<String, Object>> items = new ArrayList<>();

        List<Product> products = productMapper.selectList(null);
        if (!products.isEmpty()) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("productId", products.get(0).getId());
            item1.put("productCode", products.get(0).getProductCode());
            item1.put("productName", products.get(0).getName());
            item1.put("spec", products.get(0).getSpec());
            item1.put("quantity", 10);
            items.add(item1);

            if (products.size() > 1) {
                Map<String, Object> item2 = new HashMap<>();
                item2.put("productId", products.get(1).getId());
                item2.put("productCode", products.get(1).getProductCode());
                item2.put("productName", products.get(1).getName());
                item2.put("spec", products.get(1).getSpec());
                item2.put("quantity", 5);
                items.add(item2);
            }
        }

        order.put("items", items);
        return order;
    }

    @Override
    public List<Map<String, Object>> recommendLocations(Long productId, Integer quantity) {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
               .gt("quantity", 0)
               .orderByAsc("create_time");

        List<Inventory> inventories = inventoryMapper.selectList(wrapper);

        int remaining = quantity;
        for (Inventory inv : inventories) {
            if (remaining <= 0) break;

            Map<String, Object> rec = new HashMap<>();
            rec.put("inventoryId", inv.getId());
            rec.put("locationId", inv.getLocationId());
            rec.put("batchNo", inv.getBatchNo());
            rec.put("availableQuantity", inv.getQuantity());
            rec.put("useQuantity", Math.min(remaining, inv.getQuantity()));

            Location location = locationMapper.selectById(inv.getLocationId());
            if (location != null) {
                rec.put("locationCode", location.getLocationCode());
                rec.put("zone", location.getZone());
            }

            recommendations.add(rec);
            remaining -= (int) rec.get("useQuantity");
        }

        if (remaining > 0) {
            throw new WmsException("库存不足，缺少 " + remaining + " 件商品");
        }

        return recommendations;
    }

    @Override
    @Transactional
    public Map<String, Object> createStockOutTask(Long operatorId, String orderNo, Long productId,
                                                  String batchNo, Long locationId, Integer quantity, String remark) {
        StockOut stockOut = new StockOut();
        stockOut.setStockOutNo("OUT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        stockOut.setOperatorId(operatorId);
        stockOut.setOrderNo(orderNo);
        stockOut.setProductId(productId);
        stockOut.setBatchNo(batchNo);
        stockOut.setLocationId(locationId);
        stockOut.setQuantity(quantity);
        stockOut.setRemark(remark);
        stockOut.setStatus("PENDING");
        stockOut.setCreateTime(new Date());
        stockOut.setUpdateTime(new Date());

        save(stockOut);

        if (notificationService != null) {
            Task task = convertStockOutToTask(stockOut);
            notificationService.notifyTaskAssigned(operatorId, task);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stockOut", stockOut);
        result.put("message", "出库任务创建成功");

        return result;
    }

    @Override
    @Transactional
    public boolean completeStockOut(Long id) {
        StockOut stockOut = getById(id);
        if (stockOut == null) {
            throw new WmsException("出库任务不存在");
        }
        if (!"PENDING".equals(stockOut.getStatus())) {
            throw new WmsException("任务状态不是待出库");
        }

        String lockKey = INVENTORY_LOCK_PREFIX + stockOut.getProductId() + ":" + stockOut.getLocationId() + ":" + stockOut.getBatchNo();

        return lockService.executeWithLock(lockKey, 5, 30, () -> {
            Inventory inventory = inventoryMapper.selectOne(
                Wrappers.<Inventory>lambdaQuery()
                    .eq(Inventory::getProductId, stockOut.getProductId())
                    .eq(Inventory::getLocationId, stockOut.getLocationId())
                    .eq(Inventory::getBatchNo, stockOut.getBatchNo())
            );

            if (inventory == null || inventory.getQuantity() < stockOut.getQuantity()) {
                throw new WmsException("库存不足");
            }

            inventory.setQuantity(inventory.getQuantity() - stockOut.getQuantity());
            inventory.setUpdateTime(new Date());
            inventoryMapper.updateById(inventory);

            if (inventory.getQuantity() == 0) {
                Location location = locationMapper.selectById(stockOut.getLocationId());
                if (location != null) {
                    location.setStatus("EMPTY");
                    location.setUpdateTime(new Date());
                    locationMapper.updateById(location);
                }
                inventoryMapper.deleteById(inventory.getId());
            }

            stockOut.setStatus("COMPLETED");
            stockOut.setUpdateTime(new Date());
            updateById(stockOut);

            if (notificationService != null) {
                Task task = convertStockOutToTask(stockOut);
                notificationService.notifyTaskUpdate(task);
            }

            return true;
        });
    }

    private Task convertStockOutToTask(StockOut stockOut) {
        Task task = new Task();
        task.setTaskType("STOCK_OUT");
        task.setRelatedNo(stockOut.getStockOutNo());
        task.setOperatorId(stockOut.getOperatorId());
        task.setStatus(stockOut.getStatus());
        task.setCreateTime(stockOut.getCreateTime());
        task.setUpdateTime(stockOut.getUpdateTime());
        return task;
    }

}