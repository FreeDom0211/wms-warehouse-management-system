package com.jd.wms.service.impl;

import com.jd.wms.service.VerifyCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerifyCodeServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String generateCode(String sessionId) {
        String code = generateRandomCode();
        String key = VerifyCodeKey.PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, code, VerifyCodeKey.EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.debug("生成验证码[{}]: {}", sessionId, code);
        return code;
    }

    @Override
    public boolean validateCode(String sessionId, String code) {
        if (sessionId == null || code == null) {
            return false;
        }
        String key = VerifyCodeKey.PREFIX + sessionId;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            log.debug("验证码已过期或不存在[{}]", sessionId);
            return false;
        }
        boolean matches = storedCode.equalsIgnoreCase(code);
        if (matches) {
            redisTemplate.delete(key);
            log.debug("验证码验证成功[{}]", sessionId);
        } else {
            log.debug("验证码验证失败[{}]: 输入{} != 存储{}", sessionId, code, storedCode);
        }
        return matches;
    }

    @Override
    public void removeCode(String sessionId) {
        String key = VerifyCodeKey.PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    @Override
    public String generateCodeWithSessionId() {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode(sessionId);
        return sessionId + ":" + code;
    }

    private String generateRandomCode() {
        int code = (int) ((Math.random() * 9 + 1) * 1000);
        return String.valueOf(code);
    }

}