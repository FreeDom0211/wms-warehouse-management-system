package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.*;
import com.jd.wms.dao.mapper.*;
import com.jd.wms.service.QualityCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QualityCheckServiceImpl extends ServiceImpl<QualityCheckMapper, QualityCheck> implements QualityCheckService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public Map<String, Object> createInboundCheck(Long productId, String batchNo, Long locationId,
                                                  Integer sampleQuantity, Integer qualifiedQuantity,
                                                  Integer unqualifiedQuantity, String qualityIssue) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new WmsException("商品不存在");
        }

        String checkNo = "QC" + System.currentTimeMillis();

        QualityCheck check = new QualityCheck();
        check.setCheckNo(checkNo);
        check.setCheckType("INBOUND");
        check.setProductId(productId);
        check.setBatchNo(batchNo);
        check.setLocationId(locationId);
        check.setSampleQuantity(sampleQuantity);
        check.setQualifiedQuantity(qualifiedQuantity);
        check.setUnqualifiedQuantity(unqualifiedQuantity);
        check.setQualityIssue(qualityIssue);

        int total = qualifiedQuantity + unqualifiedQuantity;
        String result = unqualifiedQuantity == 0 ? "PASS" : "FAIL";
        check.setCheckResult(result);
        check.setStatus("COMPLETED");
        check.setCreateTime(new Date());
        check.setUpdateTime(new Date());

        save(check);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("checkId", check.getId());
        resultMap.put("checkNo", checkNo);
        resultMap.put("checkResult", result);
        resultMap.put("message", "入库抽检已完成");

        return resultMap;
    }

    @Override
    @Transactional
    public Map<String, Object> createPeriodicCheck(Long productId, Long locationId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new WmsException("商品不存在");
        }

        String checkNo = "PC" + System.currentTimeMillis();

        QualityCheck check = new QualityCheck();
        check.setCheckNo(checkNo);
        check.setCheckType("PERIODIC");
        check.setProductId(productId);
        check.setLocationId(locationId);
        check.setStatus("PENDING");
        check.setCreateTime(new Date());
        check.setUpdateTime(new Date());

        save(check);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("checkId", check.getId());
        resultMap.put("checkNo", checkNo);
        resultMap.put("message", "定期检查任务已创建");

        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getPendingChecks() {
        QueryWrapper<QualityCheck> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING")
               .orderByDesc("create_time");

        List<QualityCheck> checks = list(wrapper);
        return checks.stream().map(this::convertCheckToMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCheckList(Map<String, Object> params) {
        QueryWrapper<QualityCheck> wrapper = new QueryWrapper<>();

        if (params.get("checkType") != null && !params.get("checkType").toString().isEmpty()) {
            wrapper.eq("check_type", params.get("checkType"));
        }
        if (params.get("status") != null && !params.get("status").toString().isEmpty()) {
            wrapper.eq("status", params.get("status"));
        }
        if (params.get("productId") != null) {
            wrapper.eq("product_id", params.get("productId"));
        }

        wrapper.orderByDesc("create_time");

        List<QualityCheck> checks = list(wrapper);
        return checks.stream().map(this::convertCheckToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getCheckDetails(Long checkId) {
        QualityCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("检查记录不存在");
        }

        Map<String, Object> result = convertCheckToMap(check);

        if (check.getProductId() != null) {
            Product product = productMapper.selectById(check.getProductId());
            if (product != null) {
                result.put("productName", product.getName());
                result.put("productCode", product.getProductCode());
            }
        }

        if (check.getLocationId() != null) {
            Location location = locationMapper.selectById(check.getLocationId());
            if (location != null) {
                result.put("locationCode", location.getLocationCode());
                result.put("zone", location.getZone());
            }
        }

        return result;
    }

    @Override
    @Transactional
    public boolean submitCheckResult(Long checkId, String checkResult, String qualityIssue) {
        QualityCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("检查记录不存在");
        }

        if (!"PENDING".equals(check.getStatus())) {
            throw new WmsException("只能提交待检查状态的结果");
        }

        check.setCheckResult(checkResult);
        check.setQualityIssue(qualityIssue);
        check.setStatus("COMPLETED");
        check.setUpdateTime(new Date());

        return updateById(check);
    }

    @Override
    @Transactional
    public boolean auditCheck(Long checkId, String auditorResult, String auditorRemark) {
        QualityCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("检查记录不存在");
        }

        if (!"COMPLETED".equals(check.getStatus())) {
            throw new WmsException("只能审核已完成的检查");
        }

        check.setAuditorResult(auditorResult);
        check.setAuditorRemark(auditorRemark);
        check.setStatus("AUDITED");
        check.setUpdateTime(new Date());

        return updateById(check);
    }

    @Override
    @Transactional
    public boolean initiateReturnProcess(Long checkId) {
        QualityCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("检查记录不存在");
        }

        if (!"FAIL".equals(check.getCheckResult())) {
            throw new WmsException("只能对不合格的检查发起退货");
        }

        check.setQualityIssue("退货流程已发起: " + check.getQualityIssue());
        check.setUpdateTime(new Date());
        updateById(check);

        return true;
    }

    @Override
    @Transactional
    public boolean initiateScrapProcess(Long checkId) {
        QualityCheck check = getById(checkId);
        if (check == null) {
            throw new WmsException("检查记录不存在");
        }

        if (!"FAIL".equals(check.getCheckResult())) {
            throw new WmsException("只能对不合格的检查发起报废");
        }

        if (check.getLocationId() != null && check.getBatchNo() != null) {
            QueryWrapper<Inventory> invWrapper = new QueryWrapper<>();
            invWrapper.eq("location_id", check.getLocationId())
                     .eq("product_id", check.getProductId())
                     .eq("batch_no", check.getBatchNo());
            Inventory inventory = inventoryMapper.selectOne(invWrapper);

            if (inventory != null) {
                inventoryMapper.deleteById(inventory.getId());

                Location location = locationMapper.selectById(check.getLocationId());
                if (location != null) {
                    QueryWrapper<Inventory> checkWrapper = new QueryWrapper<>();
                    checkWrapper.eq("location_id", check.getLocationId());
                    long remaining = inventoryMapper.selectCount(checkWrapper);
                    if (remaining == 0) {
                        location.setStatus("EMPTY");
                        location.setUpdateTime(new Date());
                        locationMapper.updateById(location);
                    }
                }
            }
        }

        check.setQualityIssue("报废流程已执行: " + check.getQualityIssue());
        check.setUpdateTime(new Date());
        updateById(check);

        return true;
    }

    private Map<String, Object> convertCheckToMap(QualityCheck check) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", check.getId());
        map.put("checkNo", check.getCheckNo());
        map.put("checkType", check.getCheckType());
        map.put("checkTypeLabel", "INBOUND".equals(check.getCheckType()) ? "入库抽检" : "定期检查");
        map.put("productId", check.getProductId());
        map.put("batchNo", check.getBatchNo());
        map.put("locationId", check.getLocationId());
        map.put("sampleQuantity", check.getSampleQuantity());
        map.put("qualifiedQuantity", check.getQualifiedQuantity());
        map.put("unqualifiedQuantity", check.getUnqualifiedQuantity());
        map.put("checkResult", check.getCheckResult());
        map.put("checkResultLabel", "PASS".equals(check.getCheckResult()) ? "合格" : "不合格");
        map.put("qualityIssue", check.getQualityIssue());
        map.put("status", check.getStatus());
        map.put("statusLabel", "PENDING".equals(check.getStatus()) ? "待检查" : "COMPLETED".equals(check.getStatus()) ? "已完成" : "已审核");
        map.put("auditorId", check.getAuditorId());
        map.put("auditorResult", check.getAuditorResult());
        map.put("auditorRemark", check.getAuditorRemark());
        map.put("createTime", check.getCreateTime());
        map.put("updateTime", check.getUpdateTime());
        return map;
    }

}