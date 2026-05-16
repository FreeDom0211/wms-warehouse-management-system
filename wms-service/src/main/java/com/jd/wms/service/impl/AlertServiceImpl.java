package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.*;
import com.jd.wms.dao.mapper.*;
import com.jd.wms.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertServiceImpl extends ServiceImpl<InventoryAlertMapper, InventoryAlert> implements AlertService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public boolean setProductThreshold(Long productId, Integer minStock, Integer maxStock) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new WmsException("商品不存在");
        }

        product.setMinStock(minStock);
        product.setMaxStock(maxStock);
        product.setUpdateTime(new Date());

        return productMapper.updateById(product) > 0;
    }

    @Override
    public Map<String, Object> getAlertOverview() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<InventoryAlert> wrapper = new QueryWrapper<>();
        long total = count(wrapper);

        wrapper.eq("status", "PENDING");
        long pending = count(wrapper);

        wrapper.clear();
        wrapper.eq("status", "HANDLED");
        long handled = count(wrapper);

        wrapper.clear();
        wrapper.eq("status", "DISMISSED");
        long dismissed = count(wrapper);

        wrapper.clear();
        wrapper.eq("alert_type", "LOW_STOCK");
        long lowStock = count(wrapper);

        wrapper.clear();
        wrapper.eq("alert_type", "HIGH_STOCK");
        long highStock = count(wrapper);

        wrapper.clear();
        wrapper.eq("alert_type", "SHELF_LIFE");
        long shelfLife = count(wrapper);

        wrapper.clear();
        wrapper.eq("alert_type", "EXPIRED");
        long expired = count(wrapper);

        result.put("total", total);
        result.put("pending", pending);
        result.put("handled", handled);
        result.put("dismissed", dismissed);
        result.put("lowStock", lowStock);
        result.put("highStock", highStock);
        result.put("shelfLife", shelfLife);
        result.put("expired", expired);

        return result;
    }

    @Override
    public List<Map<String, Object>> getAlertList(Map<String, Object> params) {
        QueryWrapper<InventoryAlert> wrapper = new QueryWrapper<>();

        if (params.get("alertType") != null && !params.get("alertType").toString().isEmpty()) {
            wrapper.eq("alert_type", params.get("alertType"));
        }
        if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
            wrapper.eq("status", params.get("status"));
        }
        if (params.get("productId") != null) {
            wrapper.eq("product_id", params.get("productId"));
        }

        wrapper.orderByDesc("create_time");

        List<InventoryAlert> alerts = list(wrapper);
        return alerts.stream().map(this::convertAlertToMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPendingAlerts() {
        QueryWrapper<InventoryAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING")
               .orderByDesc("create_time");

        List<InventoryAlert> alerts = list(wrapper);
        return alerts.stream().map(this::convertAlertToMap).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean handleAlert(Long alertId, Long handlerId, String handleResult) {
        InventoryAlert alert = getById(alertId);
        if (alert == null) {
            throw new WmsException("预警记录不存在");
        }

        alert.setHandlerId(handlerId);
        alert.setHandleResult(handleResult);
        alert.setStatus("HANDLED");
        alert.setUpdateTime(new Date());

        return updateById(alert);
    }

    @Override
    @Transactional
    public boolean dismissAlert(Long alertId) {
        InventoryAlert alert = getById(alertId);
        if (alert == null) {
            throw new WmsException("预警记录不存在");
        }

        alert.setStatus("DISMISSED");
        alert.setUpdateTime(new Date());

        return updateById(alert);
    }

    @Override
    @Transactional
    public boolean checkInventoryAlerts() {
        List<Product> products = productMapper.selectList(null);

        for (Product product : products) {
            QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
            invWrapper.eq("product_id", product.getId());
            List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
            int totalQty = inventories.stream().mapToInt(Inventory::getQuantity).sum();

            QueryWrapper<InventoryAlert> alertWrapper = new QueryWrapper<>();
            alertWrapper.eq("product_id", product.getId())
                       .eq("alert_type", "LOW_STOCK")
                       .eq("status", "PENDING");
            long existingLowAlert = count(alertWrapper);

            QueryWrapper<InventoryAlert> alertWrapper2 = new QueryWrapper<>();
            alertWrapper2.eq("product_id", product.getId())
                        .eq("alert_type", "HIGH_STOCK")
                        .eq("status", "PENDING");
            long existingHighAlert = count(alertWrapper2);

            if (product.getMinStock() != null && product.getMinStock() > 0 && totalQty < product.getMinStock()) {
                if (existingLowAlert == 0) {
                    createLowStockAlert(product, totalQty);
                }
            } else if (existingLowAlert > 0) {
                removePendingAlerts(product.getId(), "LOW_STOCK");
            }

            if (product.getMaxStock() != null && product.getMaxStock() > 0 && totalQty > product.getMaxStock()) {
                if (existingHighAlert == 0) {
                    createHighStockAlert(product, totalQty);
                }
            } else if (existingHighAlert > 0) {
                removePendingAlerts(product.getId(), "HIGH_STOCK");
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean checkShelfLifeAlerts() {
        List<Product> products = productMapper.selectList(null);

        for (Product product : products) {
            if (product.getShelfLifeDays() == null || product.getShelfLifeDays() <= 0) {
                continue;
            }

            QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
            invWrapper.eq("product_id", product.getId());
            List<Inventory> inventories = inventoryMapper.selectList(invWrapper);

            for (Inventory inv : inventories) {
                if (inv.getCreateTime() == null) continue;

                Calendar createCal = Calendar.getInstance();
                createCal.setTime(inv.getCreateTime());
                createCal.add(Calendar.DAY_OF_MONTH, product.getShelfLifeDays());

                Date expireDate = createCal.getTime();
                Date now = new Date();

                long daysUntilExpire = (expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);

                QueryWrapper<InventoryAlert> alertWrapper = new QueryWrapper<>();
                alertWrapper.eq("product_id", product.getId())
                           .eq("location_id", inv.getLocationId())
                           .eq("alert_type", "EXPIRED")
                           .eq("status", "PENDING");
                long existingExpiredAlert = count(alertWrapper);

                QueryWrapper<InventoryAlert> alertWrapper2 = new QueryWrapper<>();
                alertWrapper2.eq("product_id", product.getId())
                            .eq("location_id", inv.getLocationId())
                            .eq("alert_type", "SHELF_LIFE")
                            .eq("status", "PENDING");
                long existingShelfLifeAlert = count(alertWrapper2);

                if (daysUntilExpire < 0) {
                    if (existingExpiredAlert == 0) {
                        createExpiredAlert(product, inv, Math.abs((int) daysUntilExpire));
                    }
                } else if (daysUntilExpire <= 30) {
                    if (existingShelfLifeAlert == 0) {
                        createShelfLifeAlert(product, inv, (int) daysUntilExpire);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public List<Map<String, Object>> getAlertStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        String[] types = {"LOW_STOCK", "HIGH_STOCK", "SHELF_LIFE", "EXPIRED"};
        String[] labels = {"低库存预警", "高库存预警", "临期预警", "过期预警"};

        for (int i = 0; i < types.length; i++) {
            QueryWrapper<InventoryAlert> wrapper = new QueryWrapper<>();
            wrapper.eq("alert_type", types[i]);
            long total = count(wrapper);

            wrapper.eq("status", "PENDING");
            long pending = count(wrapper);

            Map<String, Object> stat = new HashMap<>();
            stat.put("type", types[i]);
            stat.put("label", labels[i]);
            stat.put("total", total);
            stat.put("pending", pending);
            result.add(stat);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAlertTrend() {
        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
        trend.put("lowStock", Arrays.asList(5, 3, 7, 2, 4, 1, 3));
        trend.put("highStock", Arrays.asList(2, 1, 3, 0, 2, 1, 1));
        trend.put("shelfLife", Arrays.asList(8, 6, 9, 5, 7, 4, 6));
        trend.put("expired", Arrays.asList(1, 0, 2, 0, 1, 0, 1));

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(trend);
        return result;
    }

    private void createLowStockAlert(Product product, int currentQty) {
        InventoryAlert alert = new InventoryAlert();
        alert.setAlertType("LOW_STOCK");
        alert.setProductId(product.getId());
        alert.setCurrentQuantity(currentQty);
        alert.setThresholdValue(product.getMinStock());
        alert.setAlertLevel("WARNING");
        alert.setStatus("PENDING");
        alert.setDescription(String.format("商品[%s]当前库存%d低于最低库存阈值%d",
                product.getName(), currentQty, product.getMinStock()));
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        save(alert);
    }

    private void createHighStockAlert(Product product, int currentQty) {
        InventoryAlert alert = new InventoryAlert();
        alert.setAlertType("HIGH_STOCK");
        alert.setProductId(product.getId());
        alert.setCurrentQuantity(currentQty);
        alert.setThresholdValue(product.getMaxStock());
        alert.setAlertLevel("WARNING");
        alert.setStatus("PENDING");
        alert.setDescription(String.format("商品[%s]当前库存%d超过最高库存阈值%d",
                product.getName(), currentQty, product.getMaxStock()));
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        save(alert);
    }

    private void createShelfLifeAlert(Product product, Inventory inv, int daysUntilExpire) {
        Location location = locationMapper.selectById(inv.getLocationId());
        InventoryAlert alert = new InventoryAlert();
        alert.setAlertType("SHELF_LIFE");
        alert.setProductId(product.getId());
        alert.setLocationId(inv.getLocationId());
        alert.setWarehouseId(location != null ? location.getWarehouseId() : null);
        alert.setCurrentQuantity(inv.getQuantity());
        alert.setThresholdValue(30);
        alert.setAlertLevel("WARNING");
        alert.setStatus("PENDING");
        alert.setDescription(String.format("商品[%s]批次[%s]剩余保质期%d天，即将过期",
                product.getName(), inv.getBatchNo(), daysUntilExpire));
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        save(alert);
    }

    private void createExpiredAlert(Product product, Inventory inv, int daysExpired) {
        Location location = locationMapper.selectById(inv.getLocationId());
        InventoryAlert alert = new InventoryAlert();
        alert.setAlertType("EXPIRED");
        alert.setProductId(product.getId());
        alert.setLocationId(inv.getLocationId());
        alert.setWarehouseId(location != null ? location.getWarehouseId() : null);
        alert.setCurrentQuantity(inv.getQuantity());
        alert.setThresholdValue(0);
        alert.setAlertLevel("ERROR");
        alert.setStatus("PENDING");
        alert.setDescription(String.format("商品[%s]批次[%s]已过期%d天，请及时处理",
                product.getName(), inv.getBatchNo(), daysExpired));
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        save(alert);
    }

    private void removePendingAlerts(Long productId, String alertType) {
        QueryWrapper<InventoryAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
               .eq("alert_type", alertType)
               .eq("status", "PENDING");
        remove(wrapper);
    }

    private Map<String, Object> convertAlertToMap(InventoryAlert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("alertType", alert.getAlertType());
        map.put("alertTypeLabel", getAlertTypeLabel(alert.getAlertType()));
        map.put("productId", alert.getProductId());
        map.put("warehouseId", alert.getWarehouseId());
        map.put("locationId", alert.getLocationId());
        map.put("currentQuantity", alert.getCurrentQuantity());
        map.put("thresholdValue", alert.getThresholdValue());
        map.put("alertLevel", alert.getAlertLevel());
        map.put("alertLevelLabel", "WARNING".equals(alert.getAlertLevel()) ? "警告" : "错误");
        map.put("status", alert.getStatus());
        map.put("statusLabel", getStatusLabel(alert.getStatus()));
        map.put("description", alert.getDescription());
        map.put("handlerId", alert.getHandlerId());
        map.put("handleResult", alert.getHandleResult());
        map.put("createTime", alert.getCreateTime());
        map.put("updateTime", alert.getUpdateTime());

        if (alert.getProductId() != null) {
            Product product = productMapper.selectById(alert.getProductId());
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productCode", product.getProductCode());
            }
        }

        return map;
    }

    private String getAlertTypeLabel(String type) {
        switch (type) {
            case "LOW_STOCK": return "低库存预警";
            case "HIGH_STOCK": return "高库存预警";
            case "SHELF_LIFE": return "临期预警";
            case "EXPIRED": return "过期预警";
            default: return type;
        }
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case "PENDING": return "待处理";
            case "HANDLED": return "已处理";
            case "DISMISSED": return "已忽略";
            default: return status;
        }
    }

}