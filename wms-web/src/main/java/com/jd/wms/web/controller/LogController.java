package com.jd.wms.web.controller;

import com.jd.wms.common.vo.Result;
import com.jd.wms.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/log", "/api/log"})
public class LogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public Map<String, Object> getLogList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("operatorName", operatorName);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("limit", limit);
        params.put("offset", (page - 1) * limit);
        List<Map<String, Object>> logs = operationLogService.getLogList(params);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", logs.size());
        result.put("data", logs);
        return result;
    }

}
