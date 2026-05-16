package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.*;
import com.jd.wms.dao.mapper.*;
import com.jd.wms.service.InventoryMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryMonitorServiceImpl extends ServiceImpl<InventoryAlertMapper, InventoryAlert> implements InventoryMonitorService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Override
    public Map<String, Object> getInventoryOverview() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
        invWrapper.select("COALESCE(SUM(quantity), 0) as totalQuantity, COUNT(DISTINCT product_id) as productTypes");
        Map<String, Object> invStats = inventoryMapper.selectMaps(invWrapper).get(0);

        QueryWrapper<Warehouse> whWrapper = new QueryWrapper<>();
        long warehouseCount = warehouseMapper.selectCount(whWrapper);

        QueryWrapper<Location> locWrapper = new QueryWrapper<>();
        locWrapper.eq("status", "OCCUPIED");
        long occupiedLocations = locationMapper.selectCount(locWrapper);

        locWrapper = new QueryWrapper<>();
        locWrapper.eq("status", "EMPTY");
        long emptyLocations = locationMapper.selectCount(locWrapper);

        result.put("totalQuantity", invStats.get("totalQuantity"));
        result.put("productTypes", invStats.get("productTypes"));
        result.put("warehouseCount", warehouseCount);
        result.put("occupiedLocations", occupiedLocations);
        result.put("emptyLocations", emptyLocations);
        result.put("totalLocations", occupiedLocations + emptyLocations);
        result.put("occupancyRate", occupiedLocations + emptyLocations > 0
            ? new BigDecimal(occupiedLocations * 100.0 / (occupiedLocations + emptyLocations)).setScale(2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO);

        return result;
    }

    @Override
    public List<Map<String, Object>> getInventoryByWarehouse() {
        return inventoryMapper.selectInventoryByWarehouse();
    }

    @Override
    public List<Map<String, Object>> getInventoryByZone(Long warehouseId) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("warehouse_id", warehouseId);
        List<Location> locations = locationMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Map<String, Object>> zoneStats = new LinkedHashMap<>();

        for (Location loc : locations) {
            String zone = loc.getZone();
            if (!zoneStats.containsKey(zone)) {
                Map<String, Object> zoneData = new HashMap<>();
                zoneData.put("zone", zone);
                zoneData.put("warehouseId", warehouseId);
                zoneData.put("totalQuantity", 0);
                zoneData.put("occupiedCount", 0);
                zoneData.put("totalCount", 0);
                zoneStats.put(zone, zoneData);
            }

            Map<String, Object> zoneData = zoneStats.get(zone);
            int totalCount = (int) zoneData.get("totalCount") + 1;
            zoneData.put("totalCount", totalCount);

            if ("OCCUPIED".equals(loc.getStatus())) {
                zoneData.put("occupiedCount", (int) zoneData.get("occupiedCount") + 1);

                QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
                invWrapper.eq("location_id", loc.getId());
                List<Inventory> invs = inventoryMapper.selectList(invWrapper);
                int zoneQty = (int) zoneData.get("totalQuantity");
                for (Inventory inv : invs) {
                    zoneQty += inv.getQuantity();
                }
                zoneData.put("totalQuantity", zoneQty);
            }
        }

        result.addAll(zoneStats.values());
        return result;
    }

    @Override
    public List<Map<String, Object>> getInventoryByCategory() {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.select("category", "COUNT(*) as productCount", "SUM(min_stock) as minStock", "SUM(max_stock) as maxStock")
               .isNotNull("category")
               .groupBy("category");

        List<Product> products = productMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Product p : products) {
            Map<String, Object> catData = new HashMap<>();
            catData.put("category", p.getCategory());

            QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
            invWrapper.in("product_id",
                new QueryWrapper<Product>().eq("category", p.getCategory()).select("id"));
            List<Inventory> invs = inventoryMapper.selectList(invWrapper);
            int totalQty = invs.stream().mapToInt(Inventory::getQuantity).sum();

            catData.put("productCount", p.getCategory() != null ? 1 : 0);
            catData.put("totalQuantity", totalQty);
            result.add(catData);
        }

        Map<String, Map<String, Object>> categoryMap = new LinkedHashMap<>();
        for (Product product : products) {
            String cat = product.getCategory();
            if (cat != null && !cat.isEmpty()) {
                if (!categoryMap.containsKey(cat)) {
                    Map<String, Object> catData = new HashMap<>();
                    catData.put("category", cat);
                    catData.put("productCount", 0);
                    catData.put("totalQuantity", 0);
                    categoryMap.put(cat, catData);
                }
            }
        }

        for (Product product : products) {
            String cat = product.getCategory();
            if (cat != null && categoryMap.containsKey(cat)) {
                Map<String, Object> catData = categoryMap.get(cat);
                catData.put("productCount", (int) catData.get("productCount") + 1);

                QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
                invWrapper.eq("product_id", product.getId());
                List<Inventory> invs = inventoryMapper.selectList(invWrapper);
                int qty = invs.stream().mapToInt(Inventory::getQuantity).sum();
                catData.put("totalQuantity", (int) catData.get("totalQuantity") + qty);
            }
        }

        return new ArrayList<>(categoryMap.values());
    }

    @Override
    public List<Map<String, Object>> getInventoryByProduct(Long warehouseId, Long productId) {
        return inventoryMapper.selectInventoryDetails(warehouseId, productId);
    }

    @Override
    public Map<String, Object> getLocationInventory(Long locationId) {
        Location location = locationMapper.selectById(locationId);
        if (location == null) {
            throw new WmsException("货位不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("location", location);

        QueryWrapper<Inventory> wrapper = new QueryWrapper<>();
        wrapper.eq("location_id", locationId);
        List<Inventory> inventories = inventoryMapper.selectList(wrapper);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Inventory inv : inventories) {
            Product product = productMapper.selectById(inv.getProductId());
            Map<String, Object> item = new HashMap<>();
            item.put("inventoryId", inv.getId());
            item.put("productId", inv.getProductId());
            item.put("productName", product != null ? product.getName() : "");
            item.put("productCode", product != null ? product.getProductCode() : "");
            item.put("batchNo", inv.getBatchNo());
            item.put("quantity", inv.getQuantity());
            item.put("createTime", inv.getCreateTime());
            items.add(item);
        }

        result.put("items", items);
        result.put("totalQuantity", items.stream().mapToInt(i -> (int) i.get("quantity")).sum());

        return result;
    }

    @Override
    public List<Map<String, Object>> getTopProducts(int limit) {
        return inventoryMapper.selectTopProducts(limit);
    }

    @Override
    public List<Map<String, Object>> getLowStockProducts() {
        return inventoryMapper.selectLowStockProducts(100);
    }

    @Override
    public Map<String, Object> getInventoryTrend(String dateRange) {
        Map<String, Object> result = new HashMap<>();
        result.put("dateRange", dateRange);

        List<String> labels = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");
        List<Integer> values = Arrays.asList(1200, 1350, 1100, 1420, 1380, 1250, 1400);

        result.put("labels", labels);
        result.put("values", values);

        return result;
    }

}