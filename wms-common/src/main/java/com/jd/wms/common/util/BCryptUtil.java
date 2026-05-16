package com.jd.wms.common.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class BCryptUtil {

    private static final String BCRYPT_PREFIX = "$2a$10$";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    public static String encode(String rawPassword, String salt) {
        return hash(rawPassword, BCRYPT_PREFIX + salt);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith(BCRYPT_PREFIX)) {
            return BCRYPT_ENCODER.matches(rawPassword, encodedPassword);
        } else if (encodedPassword.contains(".")) {
            return verifyCustom(rawPassword, encodedPassword);
        } else {
            return verifyLegacy(rawPassword, encodedPassword);
        }
    }

    private static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt).substring(0, 22);
    }

    private static String hash(String rawPassword, String salt) {
        String combined = rawPassword + salt;
        StringBuilder hashed = new StringBuilder();
        try {
            byte[] input = combined.getBytes(StandardCharsets.UTF_8);
            byte[] digest = DigestUtils.getMd5Digest().digest(input);
            for (int round = 0; round < 10; round++) {
                digest = DigestUtils.getMd5Digest().digest(digest);
            }
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hashed.append('0');
                }
                hashed.append(hex);
            }
        } catch (Exception e) {
            throw new RuntimeException("BCrypt hash failed", e);
        }
        return salt + "." + hashed.toString();
    }

    private static boolean verifyCustom(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split("\\.");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String storedHash = parts[1];
        String computedHash = hash(rawPassword, salt).split("\\.")[1];
        return constantTimeEquals(storedHash, computedHash);
    }

    private static boolean verifyLegacy(String rawPassword, String encodedPassword) {
        String hashed = DigestUtils.md5Hex(rawPassword + "jwmsalt");
        return hashed.equals(encodedPassword);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public static void main(String[] args) {
        String password = "123456";
        String encoded = encode(password);
        System.out.println("Encoded: " + encoded);
        System.out.println("Matches: " + matches(password, encoded));
        
        String dbPassword = "$2a$10$5ZfgbeZ70SivfVb0gIq8kOVTZnBKREnlxmPLZjQhrOkGPuzPUY/PS";
        System.out.println("DB Password matches: " + matches(password, dbPassword));
    }

}