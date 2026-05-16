package com.jd.wms.common.aspect;

import com.jd.wms.common.annotation.RateLimiter;
import com.jd.wms.common.exception.WmsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    private final RedissonClient redissonClient;
    private final Map<String, RRateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

    public RateLimiterAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(com.jd.wms.common.annotation.RateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimiter annotation = method.getAnnotation(RateLimiter.class);

        String key = buildKey(annotation.key(), joinPoint);
        int limit = annotation.limit();
        int expire = annotation.expire();

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, limit, expire, RateIntervalUnit.SECONDS);

        if (!rateLimiter.tryAcquire()) {
            log.warn("请求过于频繁，被限流: key={}, limit={}", key, limit);
            throw new WmsException("请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    private String buildKey(String customKey, ProceedingJoinPoint joinPoint) {
        String userIdentifier = getUserIdentifier(joinPoint);

        if (customKey != null && !customKey.isEmpty()) {
            return "rate_limit:" + customKey + ":" + userIdentifier;
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        return "rate_limit:" + className + ":" + methodName + ":" + userIdentifier;
    }

    private String getUserIdentifier(ProceedingJoinPoint joinPoint) {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal != null) {
                return principal.getClass().getMethod("getId").invoke(principal).toString();
            }
        } catch (Exception e) {
        }

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) arg;
                String clientIp = getClientIp(request);
                if (clientIp != null && !clientIp.isEmpty()) {
                    return "ip_" + clientIp.replaceAll("\\.", "-");
                }
            }
            if (arg instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) arg;
                if (map.containsKey("username")) {
                    return "user-" + map.get("username");
                }
                if (map.containsKey("loginKey")) {
                    return "user-" + map.get("loginKey");
                }
                if (map.containsKey("phone")) {
                    return "user-" + map.get("phone");
                }
            }
        }

        return "anonymous";
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