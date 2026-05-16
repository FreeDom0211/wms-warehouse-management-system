package com.jd.wms.service;

public interface DistributedLockService {
    boolean tryLock(String key, long waitTime, long leaseTime);
    void unlock(String key);
    <T> T executeWithLock(String key, long waitTime, long leaseTime, java.util.function.Supplier<T> action);
}
