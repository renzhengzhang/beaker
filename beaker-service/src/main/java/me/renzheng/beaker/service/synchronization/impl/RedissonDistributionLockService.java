package me.renzheng.beaker.service.synchronization.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.service.synchronization.DistributionLockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedissonDistributionLockService implements DistributionLockService {

    /**
     * 默认锁过期时间（毫秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 300000L;

    /**
     * 默认获取锁超时时间（毫秒）
     */
    private static final long DEFAULT_ACQUIRE_TIMEOUT = 3000L;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        if (key == null) {
            log.warn("[RedissonDistributionLockService] tryLock failed: key cannot be null");
            return false;
        }

        RLock lock = redissonClient.getLock(key);
        long startTime = System.currentTimeMillis();

        try {
            boolean acquired = lock.tryLock(0, expireTime, timeUnit);
            long costTime = System.currentTimeMillis() - startTime;
            
            if (acquired) {
                log.info("[RedissonDistributionLockService] tryLock success: key={}, expireTime={}{}, costTime={}ms", 
                        key, expireTime, timeUnit.name().toLowerCase(), costTime);
                return true;
            } else {
                log.info("[RedissonDistributionLockService] tryLock failed: key={}, expireTime={}{}, costTime={}ms", 
                        key, expireTime, timeUnit.name().toLowerCase(), costTime);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long costTime = System.currentTimeMillis() - startTime;
            log.warn("[RedissonDistributionLockService] tryLock interrupted: key={}, costTime={}ms", key, costTime);
            return false;
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[RedissonDistributionLockService] tryLock exception: key={}, expireTime={}{}, costTime={}ms", 
                    key, expireTime, timeUnit.name().toLowerCase(), costTime, e);
            return false;
        }
    }

    @Override
    public boolean lock(String key) {
        return lock(key, DEFAULT_ACQUIRE_TIMEOUT, DEFAULT_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean lock(String key, long acquireTimeout, long expireTime, TimeUnit timeUnit) {
        if (key == null) {
            log.warn("[RedissonDistributionLockService] lock failed: key cannot be null");
            return false;
        }

        RLock lock = redissonClient.getLock(key);
        long startTime = System.currentTimeMillis();

        try {
            boolean acquired = lock.tryLock(acquireTimeout, expireTime, timeUnit);
            long costTime = System.currentTimeMillis() - startTime;
            
            if (acquired) {
                log.info("[RedissonDistributionLockService] lock success: key={}, acquireTimeout={}{}, expireTime={}{}, costTime={}ms",
                        key, acquireTimeout, timeUnit.name().toLowerCase(), 
                        expireTime, timeUnit.name().toLowerCase(), costTime);
                return true;
            } else {
                log.warn("[RedissonDistributionLockService] lock timeout: key={}, acquireTimeout={}{}, expireTime={}{}, costTime={}ms",
                        key, acquireTimeout, timeUnit.name().toLowerCase(), 
                        expireTime, timeUnit.name().toLowerCase(), costTime);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long costTime = System.currentTimeMillis() - startTime;
            log.warn("[RedissonDistributionLockService] lock interrupted: key={}, costTime={}ms", key, costTime);
            return false;
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[RedissonDistributionLockService] lock exception: key={}, acquireTimeout={}{}, expireTime={}{}, costTime={}ms", 
                    key, acquireTimeout, timeUnit.name().toLowerCase(), 
                    expireTime, timeUnit.name().toLowerCase(), costTime, e);
            return false;
        }
    }

    @Override
    public boolean unlock(String key) {
        if (key == null) {
            log.warn("[RedissonDistributionLockService] unlock failed: key cannot be null");
            return false;
        }

        RLock lock = redissonClient.getLock(key);
        long startTime = System.currentTimeMillis();

        try {
            // 检查锁是否被当前线程持有
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                long costTime = System.currentTimeMillis() - startTime;
                log.info("[RedissonDistributionLockService] unlock success: key={}, costTime={}ms", key, costTime);
                return true;
            } else {
                long costTime = System.currentTimeMillis() - startTime;
                log.warn("[RedissonDistributionLockService] unlock failed: lock not held by current thread, key={}, costTime={}ms", 
                        key, costTime);
                return false;
            }
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[RedissonDistributionLockService] unlock exception: key={}, costTime={}ms", key, costTime, e);
            return false;
        }
    }

    @Override
    public boolean isLocked(String key) {
        if (key == null) {
            log.debug("[RedissonDistributionLockService] isLocked check: key is null, return false");
            return false;
        }

        try {
            RLock lock = redissonClient.getLock(key);
            boolean locked = lock.isLocked();
            log.debug("[RedissonDistributionLockService] isLocked check: key={}, result={}", key, locked);
            return locked;
        } catch (Exception e) {
            log.error("[RedissonDistributionLockService] isLocked check exception: key={}", key, e);
            return false;
        }
    }

    @Override
    public boolean isLockedByCurrentThread(String key) {
        if (key == null) {
            log.debug("[RedissonDistributionLockService] isLockedByCurrentThread check: key is null, return false");
            return false;
        }

        try {
            RLock lock = redissonClient.getLock(key);
            boolean heldByCurrentThread = lock.isHeldByCurrentThread();
            log.debug("[RedissonDistributionLockService] isLockedByCurrentThread check: key={}, result={}", key, heldByCurrentThread);
            return heldByCurrentThread;
        } catch (Exception e) {
            log.error("[RedissonDistributionLockService] isLockedByCurrentThread check exception: key={}", key, e);
            return false;
        }
    }

    @Override
    public boolean renewLock(String key, long expireTime, TimeUnit timeUnit) {
        if (key == null) {
            log.warn("[RedissonDistributionLockService] renewLock failed: key cannot be null");
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            RLock lock = redissonClient.getLock(key);

            // 检查锁是否被当前线程持有
            if (lock.isHeldByCurrentThread()) {
                // Redisson 会自动续期，这里我们返回 true 表示续期成功
                long costTime = System.currentTimeMillis() - startTime;
                log.info("[RedissonDistributionLockService] renewLock success (auto-renewal): key={}, expireTime={}{}, costTime={}ms", 
                        key, expireTime, timeUnit.name().toLowerCase(), costTime);
                return true;
            } else {
                long costTime = System.currentTimeMillis() - startTime;
                log.warn("[RedissonDistributionLockService] renewLock failed: lock not held by current thread, key={}, costTime={}ms", 
                        key, costTime);
                return false;
            }
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[RedissonDistributionLockService] renewLock exception: key={}, expireTime={}{}, costTime={}ms", 
                    key, expireTime, timeUnit.name().toLowerCase(), costTime, e);
            return false;
        }
    }
}