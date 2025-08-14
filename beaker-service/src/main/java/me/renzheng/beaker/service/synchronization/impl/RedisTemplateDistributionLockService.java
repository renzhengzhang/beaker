package me.renzheng.beaker.service.synchronization.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.service.synchronization.DistributionLockService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisTemplateDistributionLockService implements DistributionLockService {

    /**
     * 默认锁过期时间（毫秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 300000L;

    /**
     * 默认获取锁超时时间（毫秒）
     */
    private static final long DEFAULT_ACQUIRE_TIMEOUT = 3000L;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_INTERVAL = 100L;

    /**
     * ThreadLocal 存储当前线程的 requestId
     */
    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    /**
     * 获取分布式锁的Lua脚本
     */
    private static final String LOCK_LUA_SCRIPT = """
            -- KEYS[1]: 锁的键名
            -- ARGV[1]: 锁的过期时间（毫秒）
            -- ARGV[2]: 客户端唯一标识符
            
            -- 检查锁是否存在
            if (redis.call('exists', KEYS[1]) == 0) then
                -- 锁不存在，创建新锁
                redis.call('hset', KEYS[1], ARGV[2], 1);
                redis.call('pexpire', KEYS[1], ARGV[1]);
                -- 获锁成功
                return 1;
            end
            
            -- 检查是否为同一客户端（可重入）
            if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then
                -- 同一客户端重入，增加计数
                redis.call('hincrby', KEYS[1], ARGV[2], 1);
                redis.call('pexpire', KEYS[1], ARGV[1]);
                -- 重入成功
                return 1;
            end
            
            -- 获锁失败
            return 0;
            """;

    /**
     * 释放分布式锁的 Lua 脚本
     */
    private static final String UNLOCK_LUA_SCRIPT = """
            -- KEYS[1]: 锁的键名
            -- ARGV[1]: 客户端唯一标识符
            
            -- 检查锁是否属于当前客户端
            if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then
                return 0;  -- 锁不存在或不属于当前客户端
            end
            
            -- 减少重入计数
            local counter = redis.call('hincrby', KEYS[1], ARGV[1], -1);
            if (counter > 0) then
                -- 还有重入，刷新过期时间
                redis.call('pexpire', KEYS[1], 30000);  -- 30秒默认过期时间
                -- 部分释放成功
                return 1;
            else
                -- 完全释放锁
                redis.call('del', KEYS[1]);
                -- 完全释放成功
                return 2;
            end
            """;

    /**
     * 成功返回
     */
    private static final Long SUCCESS = 1L;

    /**
     * 完全释放成功返回
     */
    private static final Long FULLY_RELEASED = 2L;

    @Resource(name = "lockRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryLock(String key, long expireTime, TimeUnit timeUnit) {
        if (key == null) {
            log.warn("tryLock failed: key cannot be null");
            return false;
        }

        String requestId = Objects.requireNonNullElse(getCurrentRequestId(), generateRequestId());
        long expireTimeMillis = timeUnit.toMillis(expireTime);
        long startTime = System.currentTimeMillis();

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(key),
                    String.valueOf(expireTimeMillis), requestId);
            long costTime = System.currentTimeMillis() - startTime;

            if (Objects.equals(result, SUCCESS)) {
                // 获取锁成功，将 requestId 存储到 ThreadLocal
                setCurrentRequestId(requestId);
                log.info("tryLock success: key={}, requestId={}, expireTime={}{}, costTime={}ms",
                        key, requestId, expireTime, timeUnit.name().toLowerCase(), costTime);
                return true;
            } else {
                log.info("tryLock failed: key={}, requestId={}, expireTime={}{}, costTime={}ms",
                        key, requestId, expireTime, timeUnit.name().toLowerCase(), costTime);
                return false;
            }
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("tryLock exception: key={}, requestId={}, expireTime={}{}, costTime={}ms",
                    key, requestId, expireTime, timeUnit.name().toLowerCase(), costTime, e);
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
            log.warn("lock failed: key cannot be null");
            return false;
        }

        String requestId = Objects.requireNonNullElse(getCurrentRequestId(), generateRequestId());
        long acquireTimeoutMillis = timeUnit.toMillis(acquireTimeout);
        long expireTimeMillis = timeUnit.toMillis(expireTime);
        long startTime = System.currentTimeMillis();
        int retryCount = 0;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_LUA_SCRIPT, Long.class);

        while (System.currentTimeMillis() - startTime < acquireTimeoutMillis) {
            retryCount++;
            long attemptStartTime = System.currentTimeMillis();

            try {
                Long result = redisTemplate.execute(script, Collections.singletonList(key),
                        String.valueOf(expireTimeMillis), requestId);
                long attemptCostTime = System.currentTimeMillis() - attemptStartTime;

                if (Objects.equals(result, SUCCESS)) {
                    // 获取锁成功，将 requestId 存储到 ThreadLocal
                    setCurrentRequestId(requestId);
                    long totalCostTime = System.currentTimeMillis() - startTime;
                    log.info("lock success: key={}, requestId={}, acquireTimeout={}{}, expireTime={}{}, retryCount={}, totalCostTime={}ms",
                            key, requestId, acquireTimeout, timeUnit.name().toLowerCase(),
                            expireTime, timeUnit.name().toLowerCase(), retryCount, totalCostTime);
                    return true;
                }

                log.debug("lock attempt failed: key={}, requestId={}, retryCount={}, attemptCostTime={}ms",
                        key, requestId, retryCount, attemptCostTime);

            } catch (Exception e) {
                long attemptCostTime = System.currentTimeMillis() - attemptStartTime;
                log.warn("lock attempt exception: key={}, requestId={}, retryCount={}, attemptCostTime={}ms",
                        key, requestId, retryCount, attemptCostTime, e);
            }

            try {
                Thread.sleep(RETRY_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                long totalCostTime = System.currentTimeMillis() - startTime;
                log.warn("lock interrupted: key={}, requestId={}, retryCount={}, totalCostTime={}ms",
                        key, requestId, retryCount, totalCostTime);
                return false;
            }
        }

        long totalCostTime = System.currentTimeMillis() - startTime;
        log.warn("lock timeout: key={}, requestId={}, acquireTimeout={}{}, retryCount={}, totalCostTime={}ms",
                key, requestId, acquireTimeout, timeUnit.name().toLowerCase(), retryCount, totalCostTime);
        return false;
    }

    @Override
    public boolean unlock(String key) {
        String requestId = getCurrentRequestId();
        if (key == null) {
            log.warn("unlock failed: key cannot be null");
            return false;
        }
        if (requestId == null) {
            log.warn("unlock failed: requestId is null, key={}", key);
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(key), requestId);
            long costTime = System.currentTimeMillis() - startTime;

            if (Objects.equals(result, SUCCESS)) {
                // 部分释放成功（还有重入）
                log.info("unlock partial success: key={}, requestId={}, costTime={}ms",
                        key, requestId, costTime);
                return true;
            } else if (Objects.equals(result, FULLY_RELEASED)) {
                // 完全释放成功，清除 ThreadLocal 中的 requestId
                clearCurrentRequestId();
                log.info("unlock fully success: key={}, requestId={}, costTime={}ms",
                        key, requestId, costTime);
                return true;
            } else {
                log.warn("unlock failed: lock not held by current thread, key={}, requestId={}, costTime={}ms",
                        key, requestId, costTime);
                return false;
            }
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("unlock exception: key={}, requestId={}, costTime={}ms", 
                    key, requestId, costTime, e);
            return false;
        }
    }

    @Override
    public boolean isLocked(String key) {
        if (key == null) {
            log.debug("isLocked check: key is null, return false");
            return false;
        }

        try {
            boolean locked = redisTemplate.hasKey(key);
            log.debug("isLocked check: key={}, result={}", key, locked);
            return locked;
        } catch (Exception e) {
            log.error("isLocked check exception: key={}", key, e);
            return false;
        }
    }

    @Override
    public boolean isLockedByCurrentThread(String key) {
        String requestId = getCurrentRequestId();
        if (key == null) {
            log.debug("isLockedByCurrentThread check: key is null, return false");
            return false;
        }
        if (requestId == null) {
            log.debug("isLockedByCurrentThread check: requestId is null, key={}, return false", key);
            return false;
        }

        try {
            boolean heldByCurrentThread = redisTemplate.opsForHash().hasKey(key, requestId);
            log.debug("isLockedByCurrentThread check: key={}, requestId={}, result={}", 
                    key, requestId, heldByCurrentThread);
            return heldByCurrentThread;
        } catch (Exception e) {
            log.error("isLockedByCurrentThread check exception: key={}, requestId={}", 
                    key, requestId, e);
            return false;
        }
    }

    @Override
    public boolean renewLock(String key, long expireTime, TimeUnit timeUnit) {
        String requestId = getCurrentRequestId();
        if (key == null) {
            log.warn("renewLock failed: key cannot be null");
            return false;
        }
        if (requestId == null) {
            log.warn("renewLock failed: requestId is null, key={}", key);
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            if (!isLockedByCurrentThread(key)) {
                long costTime = System.currentTimeMillis() - startTime;
                log.warn("renewLock failed: lock not held by current thread, key={}, requestId={}, costTime={}ms", 
                        key, requestId, costTime);
                return false;
            }

            long expireTimeMillis = timeUnit.toMillis(expireTime);
            boolean renewed = redisTemplate.expire(key, expireTimeMillis, TimeUnit.MILLISECONDS);
            long costTime = System.currentTimeMillis() - startTime;

            if (renewed) {
                log.info("renewLock success: key={}, requestId={}, expireTime={}{}, costTime={}ms", 
                        key, requestId, expireTime, timeUnit.name().toLowerCase(), costTime);
            } else {
                log.warn("renewLock failed: expire operation failed, key={}, requestId={}, costTime={}ms", 
                        key, requestId, costTime);
            }

            return renewed;
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("renewLock exception: key={}, requestId={}, expireTime={}{}, costTime={}ms", 
                    key, requestId, expireTime, timeUnit.name().toLowerCase(), costTime, e);
            return false;
        }
    }

    /**
     * 生成唯一的请求ID
     *
     * @return 请求ID
     */
    private String generateRequestId() {
        return Thread.currentThread().getId() + ":" + UUID.randomUUID();
    }

    /**
     * 设置当前线程的 requestId
     *
     * @param requestId 请求ID
     */
    private void setCurrentRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    /**
     * 获取当前线程的 requestId
     *
     * @return 请求ID，如果不存在则返回 null
     */
    private String getCurrentRequestId() {
        return REQUEST_ID_HOLDER.get();
    }

    /**
     * 清除当前线程的 requestId
     */
    private void clearCurrentRequestId() {
        REQUEST_ID_HOLDER.remove();
    }
}