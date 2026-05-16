package com.jd.wms.service.impl;

import com.jd.wms.service.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    private final RedissonClient redissonClient;

    public DistributedLockServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime) {
        try {
            RLock lock = redissonClient.getLock(key);
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (acquired) {
                log.debug("获取分布式锁成功: {}", key);
            } else {
                log.warn("获取分布式锁失败: {}", key);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁异常: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁成功: {}", key);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常: {}", key, e);
        }
    }

    @Override
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, Supplier<T> action) {
        boolean locked = tryLock(key, waitTime, leaseTime);
        if (!locked) {
            throw new com.jd.wms.common.exception.WmsException("系统繁忙，请稍后重试");
        }
        try {
            return action.get();
        } finally {
            unlock(key);
        }
    }

}
