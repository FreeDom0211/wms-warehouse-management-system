package com.jd.wms.service.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.wms.common.annotation.OperationLog;
import com.jd.wms.dao.entity.User;
import com.jd.wms.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Pointcut("@annotation(com.jd.wms.common.annotation.OperationLog)")
    public void operationLogPointcut() {}

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog annotation = method.getAnnotation(OperationLog.class);

        com.jd.wms.dao.entity.OperationLog operationLog = new com.jd.wms.dao.entity.OperationLog();
        operationLog.setModule(annotation.module());
        operationLog.setOperation(annotation.operation());
        operationLog.setMethodName(method.getName());
        operationLog.setCreateTime(new Date());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setOperatorIp(getClientIp(request));

            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String params = objectMapper.writeValueAsString(args);
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000) + "...[truncated]";
                    }
                    operationLog.setRequestParams(params);
                } catch (Exception e) {
                    operationLog.setRequestParams("解析参数失败");
                }
            }
        }

        try {
            User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
            if (currentUser != null) {
                operationLog.setOperatorId(currentUser.getId());
                operationLog.setOperatorName(currentUser.getName());
            }
        } catch (Exception e) {
            log.debug("获取当前用户失败: {}", e.getMessage());
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            operationLog.setStatus("SUCCESS");
            try {
                String response = objectMapper.writeValueAsString(result);
                if (response.length() > 2000) {
                    response = response.substring(0, 2000) + "...[truncated]";
                }
                operationLog.setResponseResult(response);
            } catch (Exception e) {
                operationLog.setResponseResult("解析响应失败");
            }
        } catch (Exception e) {
            operationLog.setStatus("FAILED");
            operationLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);
            try {
                operationLogService.save(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败: {}", e.getMessage());
            }
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}