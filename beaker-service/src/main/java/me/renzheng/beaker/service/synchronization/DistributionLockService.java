package me.renzheng.beaker.service.synchronization;

import java.util.concurrent.TimeUnit;

public interface DistributionLockService {

    /**
     * 尝试获取分布式锁
     *
     * @param key 锁的键名
     * @return 获取成功返回 true，失败返回 false
     */
    boolean tryLock(String key);

    /**
     * 尝试获取分布式锁，支持自定义锁过期时间
     *
     * @param key        锁的键名
     * @param expireTime 锁过期时间
     * @param timeUnit   时间单位
     * @return 获取成功返回 true，失败返回 false
     */
    boolean tryLock(String key, long expireTime, TimeUnit timeUnit);

    /**
     * 获取分布式锁
     *
     * @param key 锁的键名
     * @return 获取成功返回 true，失败返回 false
     */
    boolean lock(String key);

    /**
     * 获取分布式锁，支持自定义获取锁超时时间和锁过期时间
     *
     * @param key            锁的键名
     * @param acquireTimeout 获取锁超时时间
     * @param expireTime     锁过期时间
     * @param timeUnit       时间单位
     * @return 获取成功返回 true，失败返回 false
     */
    boolean lock(String key, long acquireTimeout, long expireTime, TimeUnit timeUnit);

    /**
     * 释放分布式锁
     *
     * @param key 锁的键名
     * @return 释放成功返回true，失败返回false
     */
    boolean unlock(String key);

    /**
     * 检查锁是否存在
     *
     * @param key 锁的键名
     * @return 存在返回 true，不存在返回 false
     */
    boolean isLocked(String key);

    /**
     * 检查锁是否属于当前线程
     *
     * @param key 锁的键名
     * @return 属于返回 true，不属于返回 false
     */
    boolean isLockedByCurrentThread(String key);

    /**
     * 续期锁
     *
     * @param key        锁的键名
     * @param expireTime 新的过期时间
     * @param timeUnit   时间单位
     * @return 续期成功返回 true，失败返回 false
     */
    boolean renewLock(String key, long expireTime, TimeUnit timeUnit);
}