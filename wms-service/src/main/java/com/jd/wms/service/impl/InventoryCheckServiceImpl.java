package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.*;
import com.jd.wms.dao.mapper.*;
import com.jd.wms.service.InventoryCheckService;
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
public class InventoryCheckServiceImpl extends ServiceImpl<InventoryCheckMapper, InventoryCheck> implements InventoryCheckService {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ExceptionReportMapper exceptionReportMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public InventoryCheck getByCheckNo(String checkNo) {
        return baseMapper.selectByCheckNo(checkNo);
    }

    @Override
    @Transactional
    public boolean addInventoryCheck(InventoryCheck check) {
        check.setCheckNo("CHECK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        check.setStatus("PENDING");
        check.setCreateTime(new Date());
        check.setUpdateTime(new Date());
        return save(check);
    }

    @Override
    @Transactional
    public boolean updateInventoryCheck(InventoryCheck check) {
        check.setUpdateTime(new Date());
        return updateById(check);
    }

    @Override
    @Transactional
    public boolean deleteInventoryCheck(Long id) {
        InventoryCheck check = getById(id);
        if (check == null) {
            throw new WmsException("盘点单不存在");
        }
        return removeById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> createCheckTask(Long operatorId, String zone) {
        InventoryCheck check = new InventoryCheck();
        check.setCheckNo("CHECK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        check.setZone(zone);
        check.setOperatorId(operatorId);
        check.setStatus("PENDING");
        check.setCreateTime(new Date());
        check.setUpdateTime(new Date());
        
        save(check);

        Task task = new Task();
        task.setTaskType("INVENTORY_CHECK");
        task.setRelatedNo(check.getCheckNo());
        task.setOperatorId(operatorId);
        task.setStatus("PENDING");
        task.setPriority(2);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        taskMapper.insert(task);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("checkNo", check.getCheckNo());
        result.put("message", "盘点任务创建成功");
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getCheckList(Long operatorId) {
        QueryWrapper<InventoryCheck> wrapper = new QueryWrapper<>();
        wrapper.eq("operator_id", operatorId).orderByDesc("create_time");
        List<InventoryCheck> checks = baseMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (InventoryCheck check : checks) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", check.getId());
            map.put("checkNo", check.getCheckNo());
            map.put("zone", check.getZone());
            map.put("status", check.getStatus());
            map.put("createTime", check.getCreateTime());
            map.put("updateTime", check.getUpdateTime());
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> getCheckDetails(Long checkId) {
        InventoryCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("盘点单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("check", check);

        QueryWrapper<Location> locWrapper = new QueryWrapper<>();
        if (check.getZone() != null && !check.getZone().isEmpty()) {
            locWrapper.eq("zone", check.getZone());
        }
        List<Location> locations = locationMapper.selectList(locWrapper);

        List<Map<String, Object>> inventoryList = new ArrayList<>();
        for (Location loc : locations) {
            QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
            invWrapper.eq("location_id", loc.getId());
            List<Inventory> inventories = inventoryMapper.selectList(invWrapper);

            for (Inventory inv : inventories) {
                Product product = productMapper.selectById(inv.getProductId());
                
                Map<String, Object> item = new HashMap<>();
                item.put("inventoryId", inv.getId());
                item.put("locationId", loc.getId());
                item.put("locationCode", loc.getLocationCode());
                item.put("zone", loc.getZone());
                item.put("productId", inv.getProductId());
                item.put("productCode", product != null ? product.getProductCode() : "");
                item.put("productName", product != null ? product.getName() : "");
                item.put("batchNo", inv.getBatchNo());
                item.put("systemQuantity", inv.getQuantity());
                item.put("actualQuantity", 0);
                item.put("difference", 0);
                inventoryList.add(item);
            }
        }

        result.put("items", inventoryList);
        return result;
    }

    @Override
    @Transactional
    public boolean submitCheckResult(Long checkId, List<Map<String, Object>> actualQuantities) {
        InventoryCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("盘点单不存在");
        }
        if (!"PENDING".equals(check.getStatus())) {
            throw new WmsException("盘点单状态不正确");
        }

        List<String> differences = new ArrayList<>();

        for (Map<String, Object> item : actualQuantities) {
            Long inventoryId = Long.parseLong(item.get("inventoryId").toString());
            Integer systemQty = Integer.parseInt(item.get("systemQuantity").toString());
            Integer actualQty = Integer.parseInt(item.get("actualQuantity").toString());

            Inventory inventory = inventoryMapper.selectById(inventoryId);
            if (inventory != null) {
                int diff = actualQty - systemQty;
                if (diff != 0) {
                    differences.add("货位" + (String) item.get("locationCode") + 
                                  " 商品" + (String) item.get("productName") + 
                                  " 差异:" + diff);

                    inventory.setQuantity(actualQty);
                    inventory.setUpdateTime(new Date());
                    inventoryMapper.updateById(inventory);
                }
            }
        }

        if (!differences.isEmpty()) {
            ExceptionReport report = new ExceptionReport();
            report.setReporterId(check.getOperatorId());
            report.setExceptionType("DIFF");
            report.setDescription(String.join("; ", differences));
            report.setStatus("PENDING");
            report.setCreateTime(new Date());
            report.setUpdateTime(new Date());
            exceptionReportMapper.insert(report);

            check.setDiffDescription(String.join("; ", differences));
        }

        check.setStatus("COMPLETED");
        check.setUpdateTime(new Date());
        updateById(check);

        Task task = taskMapper.selectOne(
            Wrappers.<Task>lambdaQuery()
                .eq(Task::getRelatedNo, check.getCheckNo())
        );
        if (task != null) {
            task.setStatus("COMPLETED");
            task.setUpdateTime(new Date());
            taskMapper.updateById(task);
        }

        return true;
    }

}