package me.renzheng.beaker.service.synchronization;

import jakarta.annotation.Resource;
import me.renzheng.beaker.service.synchronization.impl.RedisTemplateDistributionLockService;
import me.renzheng.beaker.start.AbstractTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RedisTemplateDistributionLockService 单元测试
 */
@DisplayName("RedisTemplateDistributionLockService 测试")
public class RedisTemplateDistributionLockServiceTests extends AbstractTests {

    @Resource
    private RedisTemplateDistributionLockService lockService;

    @Resource(name = "cacheRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        Set<String> keys = redisTemplate.keys("test:lock:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // region 基础锁操作测试

    @Test
    @DisplayName("当尝试获取锁时_应该成功获取")
    void whenTryLock_thenShouldSucceed() {
        // Given
        String key = "test:lock:basic";

        // When
        boolean acquired = lockService.tryLock(key);

        // Then
        assertTrue(acquired);
        assertTrue(lockService.isLocked(key));
        assertTrue(lockService.isLockedByCurrentThread(key));

        // Cleanup
        lockService.unlock(key);
    }

    @Test
    @DisplayName("当锁已被占用时_尝试获取锁应该失败")
    void whenLockAlreadyTaken_thenTryLockShouldFail() throws InterruptedException {
        // Given
        String key = "test:lock:conflict";

        // 在另一个线程中获取锁
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            boolean acquired = lockService.tryLock(key, 2, TimeUnit.SECONDS);
            assertTrue(acquired);
            try {
                Thread.sleep(1000); // 持有锁1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockService.unlock(key);
            }
        });

        // 等待锁被获取
        Thread.sleep(100);

        // When - 在当前线程尝试获取锁
        boolean secondAcquired = lockService.tryLock(key);

        // Then
        assertFalse(secondAcquired);
        assertTrue(lockService.isLocked(key));
        assertFalse(lockService.isLockedByCurrentThread(key));

        // 等待第一个线程完成
        future.join();
    }

    @Test
    @DisplayName("当释放锁时_应该成功释放")
    void whenUnlock_thenShouldSucceed() {
        // Given
        String key = "test:lock:unlock";
        boolean acquired = lockService.tryLock(key);
        assertTrue(acquired);

        // When
        boolean unlocked = lockService.unlock(key);

        // Then
        assertTrue(unlocked);
        assertFalse(lockService.isLocked(key));
        assertFalse(lockService.isLockedByCurrentThread(key));
    }

    @Test
    @DisplayName("当释放不属于当前线程的锁时_应该失败")
    void whenUnlockNotOwnedLock_thenShouldFail() throws InterruptedException {
        // Given
        String key = "test:lock:wrong_thread";

        // 在另一个线程中获取锁
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            boolean acquired = lockService.tryLock(key, 2, TimeUnit.SECONDS);
            assertTrue(acquired);
            try {
                Thread.sleep(1500); // 持有锁1.5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockService.unlock(key);
            }
        });

        // 等待锁被获取
        Thread.sleep(100);

        // When - 在当前线程尝试释放锁
        boolean unlocked = lockService.unlock(key);

        // Then
        assertFalse(unlocked);
        assertTrue(lockService.isLocked(key));
        assertFalse(lockService.isLockedByCurrentThread(key));

        // 等待第一个线程完成
        future.join();
    }

    // endregion

    // region 可重入锁测试

    @Test
    @DisplayName("当同一线程多次获取锁时_应该支持可重入")
    void whenSameThreadAcquireLockMultipleTimes_thenShouldSupportReentrant() {
        // Given
        String key = "test:lock:reentrant";

        // When
        boolean acquired1 = lockService.tryLock(key);
        boolean acquired2 = lockService.tryLock(key);

        // Then
        assertTrue(acquired1);
        assertTrue(acquired2);
        assertTrue(lockService.isLocked(key));
        assertTrue(lockService.isLockedByCurrentThread(key));

        // Cleanup - 需要释放两次（可重入）
        lockService.unlock(key);
        lockService.unlock(key);
    }

    @Test
    @DisplayName("当可重入锁部分释放时_锁应该仍然存在")
    void whenReentrantLockPartiallyReleased_thenLockShouldStillExist() {
        // Given
        String key = "test:lock:reentrant_partial";
        boolean acquired1 = lockService.tryLock(key);
        boolean acquired2 = lockService.tryLock(key);
        assertTrue(acquired1);
        assertTrue(acquired2);

        // When - 释放一次
        boolean firstUnlock = lockService.unlock(key);

        // Then
        assertTrue(firstUnlock);
        assertTrue(lockService.isLocked(key)); // 锁仍然存在
        assertTrue(lockService.isLockedByCurrentThread(key));

        // When - 再次释放
        boolean secondUnlock = lockService.unlock(key);

        // Then
        assertTrue(secondUnlock);
        assertFalse(lockService.isLocked(key)); // 锁完全释放
    }

    // endregion

    // region 带超时的锁操作测试

    @Test
    @DisplayName("当使用自定义过期时间获取锁时_应该正确设置过期时间")
    void whenTryLockWithCustomExpireTime_thenShouldSetCorrectExpiration() {
        // Given
        String key = "test:lock:expire";
        long expireTime = 1;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        // When
        boolean acquired = lockService.tryLock(key, expireTime, timeUnit);

        // Then
        assertTrue(acquired);
        assertTrue(lockService.isLocked(key));

        // 验证过期时间设置正确（允许一定误差）
        long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        assertTrue(ttl > 0 && ttl <= 1000);
    }

    @Test
    @DisplayName("当阻塞获取锁时_应该在超时时间内获取到锁")
    void whenLockWithTimeout_thenShouldAcquireWithinTimeout() {
        // Given
        String key = "test:lock:blocking";
        long acquireTimeout = 2000;
        long expireTime = 5000;

        // When
        long startTime = System.currentTimeMillis();
        boolean acquired = lockService.lock(key, acquireTimeout, expireTime, TimeUnit.MILLISECONDS);
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(acquired);
        assertTrue(lockService.isLocked(key));
        assertTrue(duration < acquireTimeout); // 应该很快获取到锁

        // Cleanup
        lockService.unlock(key);
    }

    @Test
    @DisplayName("当阻塞获取锁超时时_应该返回false")
    void whenLockTimeout_thenShouldReturnFalse() throws InterruptedException {
        // Given
        String key = "test:lock:timeout";

        // 在另一个线程中先获取锁
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            boolean acquired = lockService.tryLock(key, 5, TimeUnit.SECONDS);
            assertTrue(acquired);
            try {
                Thread.sleep(1000); // 持有锁1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockService.unlock(key);
            }
        });

        // 等待锁被获取
        Thread.sleep(100);

        // When - 另一个线程尝试获取锁，但会超时
        long startTime = System.currentTimeMillis();
        boolean acquired = lockService.lock(key, 500, 5000, TimeUnit.MILLISECONDS);
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertFalse(acquired);
        assertTrue(duration >= 500); // 应该等待了超时时间

        // 等待第一个线程完成
        future.join();
    }

    // endregion

    // region 锁续期测试

    @Test
    @DisplayName("当续期锁时_应该成功延长过期时间")
    void whenRenewLock_thenShouldExtendExpiration() {
        // Given
        String key = "test:lock:renew";
        boolean acquired = lockService.tryLock(key, 1, TimeUnit.SECONDS);
        assertTrue(acquired);

        // When
        boolean renewed = lockService.renewLock(key, 5, TimeUnit.SECONDS);

        // Then
        assertTrue(renewed);
        assertTrue(lockService.isLocked(key));

        // 验证过期时间被延长
        long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertTrue(ttl > 1); // 应该大于原来的1秒

        // Cleanup
        lockService.unlock(key);
    }

    @Test
    @DisplayName("当续期不属于当前线程的锁时_应该失败")
    void whenRenewLockNotOwnedByCurrentThread_thenShouldFail() throws InterruptedException {
        // Given
        String key = "test:lock:renew_wrong";

        // 在另一个线程中获取锁
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            boolean acquired = lockService.tryLock(key, 2, TimeUnit.SECONDS);
            assertTrue(acquired);
            try {
                Thread.sleep(1500); // 持有锁1.5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockService.unlock(key);
            }
        });

        // 等待锁被获取
        Thread.sleep(100);

        // When - 在当前线程尝试续期
        boolean renewed = lockService.renewLock(key, 5, TimeUnit.SECONDS);

        // Then
        assertFalse(renewed);

        // 等待第一个线程完成
        future.join();
    }

    // endregion

    // region 并发测试

    @Test
    @DisplayName("当多线程并发获取锁时_只有一个线程应该成功")
    void whenConcurrentLockAcquisition_thenOnlyOneThreadShouldSucceed() throws InterruptedException {
        // Given
        String key = "test:lock:concurrent";
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    startLatch.await();
                    boolean acquired = lockService.tryLock(key);
                    if (acquired) {
                        successCount.incrementAndGet();
                        // 持有锁一段时间
                        Thread.sleep(100);
                        lockService.unlock(key);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 启动所有线程
        doneLatch.await(5, TimeUnit.SECONDS); // 等待所有线程完成

        // Then
        assertEquals(1, successCount.get()); // 只有一个线程应该成功获取锁
        assertFalse(lockService.isLocked(key)); // 锁应该已被释放
    }

    @Test
    @DisplayName("当多线程顺序获取和释放锁时_应该正确处理")
    void whenSequentialLockOperations_thenShouldHandleCorrectly() throws InterruptedException {
        // Given
        String key = "test:lock:sequential";
        int threadCount = 5;
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            CompletableFuture.runAsync(() -> {
                try {
                    // 每个线程延迟不同时间启动，避免完全并发
                    Thread.sleep(threadIndex * 50);

                    boolean acquired = lockService.lock(key, 1000, 2000, TimeUnit.MILLISECONDS);
                    if (acquired) {
                        successCount.incrementAndGet();
                        // 持有锁一段时间
                        Thread.sleep(100);
                        lockService.unlock(key);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        doneLatch.await(10, TimeUnit.SECONDS);

        // Then
        assertEquals(threadCount, successCount.get()); // 所有线程都应该成功获取锁
        assertFalse(lockService.isLocked(key)); // 最终锁应该被释放
    }

    // endregion

    // region 边界条件测试

    @Test
    @DisplayName("当传入null参数时_应该正确处理")
    void whenPassNullParameters_thenShouldHandleGracefully() {
        // When & Then
        assertFalse(lockService.tryLock(null));
        assertFalse(lockService.unlock(null));
        assertFalse(lockService.isLocked(null));
        assertFalse(lockService.isLockedByCurrentThread(null));
        assertFalse(lockService.renewLock(null, 1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("当锁不存在时_检查操作应该返回false")
    void whenLockNotExists_thenCheckOperationsShouldReturnFalse() {
        // Given
        String nonExistentKey = "test:lock:nonexistent";

        // When & Then
        assertFalse(lockService.isLocked(nonExistentKey));
        assertFalse(lockService.isLockedByCurrentThread(nonExistentKey));
        assertFalse(lockService.unlock(nonExistentKey));
        assertFalse(lockService.renewLock(nonExistentKey, 1, TimeUnit.SECONDS));
    }

    // endregion
}