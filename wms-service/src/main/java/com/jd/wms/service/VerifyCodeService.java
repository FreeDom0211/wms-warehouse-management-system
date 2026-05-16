package com.jd.wms.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface VerifyCodeService {

    String generateCode(String sessionId);

    boolean validateCode(String sessionId, String code);

    void removeCode(String sessionId);

    String generateCodeWithSessionId();

    class VerifyCodeKey {
        public static final String PREFIX = "verify:code:";
        public static final long EXPIRE_MINUTES = 5;
    }

}