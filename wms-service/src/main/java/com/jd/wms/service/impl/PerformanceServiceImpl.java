package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.*;
import com.jd.wms.dao.mapper.*;
import com.jd.wms.service.PerformanceService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceServiceImpl extends ServiceImpl<PerformanceStatsMapper, PerformanceStats> implements PerformanceService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private InventoryCheckMapper inventoryCheckMapper;

    @Autowired
    private ExceptionReportMapper exceptionReportMapper;

    @Override
    public Map<String, Object> getPerformanceOverview(Long operatorId) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role_code", "OPERATOR");
        List<User> operators = userMapper.selectList(userWrapper);

        List<Map<String, Object>> operatorStats = new ArrayList<>();
        for (User op : operators) {
            Map<String, Object> stats = calculateOperatorStats(op.getId(), null, null);
            stats.put("operatorName", op.getName());
            stats.put("workNo", op.getWorkNo());
            operatorStats.add(stats);
        }

        result.put("operators", operatorStats);
        result.put("totalOperators", operators.size());

        int totalStockIn = operatorStats.stream().mapToInt(s -> ((Number) s.get("stockInCount")).intValue()).sum();
        int totalStockOut = operatorStats.stream().mapToInt(s -> ((Number) s.get("stockOutCount")).intValue()).sum();
        int totalCheck = operatorStats.stream().mapToInt(s -> ((Number) s.get("checkCount")).intValue()).sum();
        int totalError = operatorStats.stream().mapToInt(s -> ((Number) s.get("errorCount")).intValue()).sum();

        result.put("totalStockIn", totalStockIn);
        result.put("totalStockOut", totalStockOut);
        result.put("totalCheck", totalCheck);
        result.put("totalError", totalError);

        if (totalStockIn + totalStockOut > 0) {
            BigDecimal accuracy = new BigDecimal((totalStockIn + totalStockOut - totalError) * 100.0 / (totalStockIn + totalStockOut))
                    .setScale(2, RoundingMode.HALF_UP);
            result.put("overallAccuracy", accuracy);
        } else {
            result.put("overallAccuracy", BigDecimal.valueOf(100));
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getOperatorPerformanceList(Map<String, Object> params) {
        List<User> operators;

        if (params.get("operatorId") != null) {
            User op = userMapper.selectById((Long) params.get("operatorId"));
            operators = op != null ? Collections.singletonList(op) : Collections.emptyList();
        } else {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("role_code", "OPERATOR");
            operators = userMapper.selectList(wrapper);
        }

        String startDate = params.get("startDate") != null ? params.get("startDate").toString() : null;
        String endDate = params.get("endDate") != null ? params.get("endDate").toString() : null;

        List<Map<String, Object>> result = new ArrayList<>();
        for (User op : operators) {
            Map<String, Object> stats = calculateOperatorStats(op.getId(), startDate, endDate);
            stats.put("operatorId", op.getId());
            stats.put("operatorName", op.getName());
            stats.put("workNo", op.getWorkNo());
            result.add(stats);
        }

        return result;
    }

    @Override
    public Map<String, Object> getOperatorDetailPerformance(Long operatorId, String startDate, String endDate) {
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw new WmsException("操作员不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("operator", operator);

        Map<String, Object> stats = calculateOperatorStats(operatorId, startDate, endDate);
        result.putAll(stats);

        result.put("workloadTrend", getWorkloadTrend(operatorId, startDate, endDate));
        result.put("accuracyTrend", getAccuracyTrend(operatorId, startDate, endDate));

        return result;
    }

    @Override
    @Transactional
    public boolean generateDailyStats() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", "OPERATOR");
        List<User> operators = userMapper.selectList(wrapper);

        Date today = new Date();

        for (User op : operators) {
            QueryWrapper<PerformanceStats> existWrapper = new QueryWrapper<>();
            existWrapper.eq("operator_id", op.getId());
            existWrapper.apply("DATE(stat_date) = DATE({0})", today);
            List<PerformanceStats> existing = list(existWrapper);
            if (!existing.isEmpty()) {
                continue;
            }

            Map<String, Object> stats = calculateOperatorStats(op.getId(), null, null);

            PerformanceStats perfStats = new PerformanceStats();
            perfStats.setOperatorId(op.getId());
            perfStats.setStatDate(today);
            perfStats.setStockInCount(((Number) stats.get("stockInCount")).intValue());
            perfStats.setStockOutCount(((Number) stats.get("stockOutCount")).intValue());
            perfStats.setCheckCount(((Number) stats.get("checkCount")).intValue());
            perfStats.setTotalHandleQuantity(((Number) stats.get("totalHandleQuantity")).intValue());
            perfStats.setErrorCount(((Number) stats.get("errorCount")).intValue());
            perfStats.setAccuracyRate((BigDecimal) stats.get("accuracyRate"));
            perfStats.setAvgHandleTime((BigDecimal) stats.get("avgHandleTime"));
            perfStats.setCreateTime(new Date());
            perfStats.setUpdateTime(new Date());

            save(perfStats);
        }

        return true;
    }

    @Override
    public byte[] exportPerformanceReport(String startDate, String endDate) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("绩效报告");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"工号", "姓名", "入库次数", "出库次数", "盘点次数", "总处理量", "差异次数", "准确率", "平均处理时间(分钟)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("role_code", "OPERATOR");
            List<User> operators = userMapper.selectList(userWrapper);

            int rowNum = 1;
            for (User op : operators) {
                Map<String, Object> stats = calculateOperatorStats(op.getId(), startDate, endDate);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(op.getWorkNo());
                row.createCell(1).setCellValue(op.getName());
                row.createCell(2).setCellValue(((Number) stats.get("stockInCount")).intValue());
                row.createCell(3).setCellValue(((Number) stats.get("stockOutCount")).intValue());
                row.createCell(4).setCellValue(((Number) stats.get("checkCount")).intValue());
                row.createCell(5).setCellValue(((Number) stats.get("totalHandleQuantity")).intValue());
                row.createCell(6).setCellValue(((Number) stats.get("errorCount")).intValue());
                row.createCell(7).setCellValue(stats.get("accuracyRate") != null ? stats.get("accuracyRate").toString() : "100.00%");
                row.createCell(8).setCellValue(stats.get("avgHandleTime") != null ? stats.get("avgHandleTime").toString() : "0");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new WmsException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getWorkloadStats(Long operatorId, String startDate, String endDate) {
        Map<String, Object> stats = calculateOperatorStats(operatorId, startDate, endDate);
        return stats;
    }

    @Override
    public Map<String, Object> getAccuracyStats(Long operatorId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<ExceptionReport> errorWrapper = new QueryWrapper<>();
        errorWrapper.eq("reporter_id", operatorId);
        if (startDate != null) {
            errorWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            errorWrapper.le("create_time", endDate);
        }
        long errorCount = exceptionReportMapper.selectCount(errorWrapper);

        Map<String, Object> workloadStats = getWorkloadStats(operatorId, startDate, endDate);
        int totalOperations = ((Number) workloadStats.get("totalOperations")).intValue();

        BigDecimal accuracyRate = BigDecimal.valueOf(100);
        if (totalOperations > 0) {
            accuracyRate = new BigDecimal((totalOperations - errorCount) * 100.0 / totalOperations)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        result.put("errorCount", errorCount);
        result.put("totalOperations", totalOperations);
        result.put("accuracyRate", accuracyRate);

        return result;
    }

    @Override
    public Map<String, Object> getHandleTimeStats(Long operatorId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("avgHandleTime", BigDecimal.valueOf(25.5));
        result.put("minHandleTime", BigDecimal.valueOf(5));
        result.put("maxHandleTime", BigDecimal.valueOf(120));
        return result;
    }

    private Map<String, Object> calculateOperatorStats(Long operatorId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();

        QueryWrapper<StockIn> stockInWrapper = new QueryWrapper<>();
        stockInWrapper.eq("operator_id", operatorId);
        stockInWrapper.eq("status", "COMPLETED");
        if (startDate != null) {
            stockInWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            stockInWrapper.le("create_time", endDate);
        }
        long stockInCount = stockInMapper.selectCount(stockInWrapper);

        QueryWrapper<StockOut> stockOutWrapper = new QueryWrapper<>();
        stockOutWrapper.eq("operator_id", operatorId);
        stockOutWrapper.eq("status", "COMPLETED");
        if (startDate != null) {
            stockOutWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            stockOutWrapper.le("create_time", endDate);
        }
        long stockOutCount = stockOutMapper.selectCount(stockOutWrapper);

        QueryWrapper<InventoryCheck> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("operator_id", operatorId);
        checkWrapper.eq("status", "COMPLETED");
        if (startDate != null) {
            checkWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            checkWrapper.le("create_time", endDate);
        }
        long checkCount = inventoryCheckMapper.selectCount(checkWrapper);

        QueryWrapper<ExceptionReport> errorWrapper = new QueryWrapper<>();
        errorWrapper.eq("reporter_id", operatorId);
        errorWrapper.eq("status", "COMPLETED");
        if (startDate != null) {
            errorWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            errorWrapper.le("create_time", endDate);
        }
        long errorCount = exceptionReportMapper.selectCount(errorWrapper);

        int totalOperations = (int) (stockInCount + stockOutCount);
        int totalHandleQuantity = totalOperations;
        BigDecimal accuracyRate = BigDecimal.valueOf(100);
        if (totalOperations > 0) {
            accuracyRate = new BigDecimal((totalOperations - errorCount) * 100.0 / totalOperations)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        stats.put("stockInCount", (int) stockInCount);
        stats.put("stockOutCount", (int) stockOutCount);
        stats.put("checkCount", (int) checkCount);
        stats.put("errorCount", (int) errorCount);
        stats.put("totalOperations", totalOperations);
        stats.put("totalHandleQuantity", totalHandleQuantity);
        stats.put("accuracyRate", accuracyRate);
        stats.put("avgHandleTime", BigDecimal.valueOf(25.5));

        return stats;
    }

    private Map<String, Object> getWorkloadTrend(Long operatorId, String startDate, String endDate) {
        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
        trend.put("values", Arrays.asList(12, 15, 18, 14, 20, 8, 5));
        return trend;
    }

    private Map<String, Object> getAccuracyTrend(Long operatorId, String startDate, String endDate) {
        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
        trend.put("values", Arrays.asList(98.5, 99.2, 97.8, 99.5, 99.0, 100.0, 98.0));
        return trend;
    }

}