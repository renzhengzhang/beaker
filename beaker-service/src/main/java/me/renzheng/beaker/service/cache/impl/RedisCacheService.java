package me.renzheng.beaker.service.cache.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.common.exception.BusinessException;
import me.renzheng.beaker.service.cache.CacheService;
import me.renzheng.beaker.service.cache.CacheWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisCacheService implements CacheService {

    @Resource(name = "cacheRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        executeWithExceptionHandling(() -> {
            redisTemplate.opsForValue().set(key, value);
            return null;
        }, "Redis set operation failed for key: " + key);
    }

    @Override
    public void set(String key, Object value, Duration timeout) {
        executeWithExceptionHandling(() -> {
            redisTemplate.opsForValue().set(key, value, timeout);
            return null;
        }, "Redis set operation with duration failed for key: " + key);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        executeWithExceptionHandling(() -> {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return null;
        }, "Redis set operation with timeout failed for key: " + key);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return executeWithExceptionHandling(() -> {
            Object value = redisTemplate.opsForValue().get(key);
            return convertValue(value, clazz);
        }, "Redis get operation failed for key: " + key);
    }

    @Override
    public String getString(String key) {
        return get(key, String.class);
    }

    @Override
    public Boolean delete(String key) {
        return executeWithExceptionHandling(() ->
            redisTemplate.delete(key),
            "Redis delete operation failed for key: " + key
        );
    }

    @Override
    public Long delete(String... keys) {
        return executeWithExceptionHandling(() ->
            redisTemplate.delete(List.of(keys)),
            "Redis delete operation failed for keys: " + List.of(keys)
        );
    }

    @Override
    public Boolean exists(String key) {
        return executeWithExceptionHandling(() ->
            redisTemplate.hasKey(key),
            "Redis exists operation failed for key: " + key
        );
    }

    @Override
    public Boolean expire(String key, Duration timeout) {
        return executeWithExceptionHandling(() ->
            redisTemplate.expire(key, timeout),
            "Redis expire operation failed for key: " + key
        );
    }

    @Override
    public <T> void multiSet(Map<String, T> keyValues) {
        executeWithExceptionHandling(() -> {
            Map<String, Object> objectMap = new HashMap<>(keyValues);
            redisTemplate.opsForValue().multiSet(objectMap);
            return null;
        }, "Redis multiSet operation failed");
    }

    @Override
    public <T> void multiSet(Map<String, T> keyValues, Duration timeout) {
        executeWithExceptionHandling(() -> {
            Map<String, Object> objectMap = new HashMap<>(keyValues);
            redisTemplate.opsForValue().multiSet(objectMap);
            // 为每个 key 设置过期时间
            for (String key : keyValues.keySet()) {
                redisTemplate.expire(key, timeout);
            }
            return null;
        }, "Redis multiSet with timeout operation failed");
    }

    @Override
    public <T> List<T> multiGet(List<String> keys, Class<T> clazz) {
        return executeWithExceptionHandling(() -> {
            List<Object> values = redisTemplate.opsForValue().multiGet(keys);
            List<T> result = new ArrayList<>();

            if (values != null) {
                for (Object value : values) {
                    result.add(convertValue(value, clazz));
                }
            }

            return result;
        }, "Redis multiGet operation failed for keys: " + keys);
    }

    @Override
    public void hSet(String key, String field, Object value) {
        executeWithExceptionHandling(() -> {
            redisTemplate.opsForHash().put(key, field, value);
            return null;
        }, "Redis hSet operation failed for key: " + key + ", field: " + field);
    }

    @Override
    public <T> T hGet(String key, String field, Class<T> clazz) {
        return executeWithExceptionHandling(() -> {
            Object value = redisTemplate.<String, Object>opsForHash().get(key, field);
            return convertValue(value, clazz);
        }, "Redis hGet operation failed for key: " + key + ", field: " + field);
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        return executeWithExceptionHandling(() ->
            redisTemplate.<String, Object>opsForHash().entries(key),
            "Redis hGetAll operation failed for key: " + key
        );
    }

    @Override
    public Boolean hExists(String key, String field) {
        return executeWithExceptionHandling(() ->
            redisTemplate.<String, Object>opsForHash().hasKey(key, field),
            "Redis hExists operation failed for key: " + key + ", field: " + field
        );
    }

    @Override
    public Long hDelete(String key, String... fields) {
        return executeWithExceptionHandling(() ->
            redisTemplate.<String, Object>opsForHash().delete(key, (Object[]) fields),
            "Redis hDelete operation failed for key: " + key + ", fields: " + List.of(fields)
        );
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForSet().add(key, values),
            "Redis sAdd operation failed for key: " + key
        );
    }

    @Override
    public Set<Object> sMembers(String key) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForSet().members(key),
            "Redis sMembers operation failed for key: " + key
        );
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForSet().isMember(key, value),
            "Redis sIsMember operation failed for key: " + key
        );
    }

    @Override
    public Long sRemove(String key, Object... values) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForSet().remove(key, values),
            "Redis sRemove operation failed for key: " + key
        );
    }

    @Override
    public Long lPush(String key, Object... values) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForList().leftPushAll(key, values),
            "Redis lPush operation failed for key: " + key
        );
    }

    @Override
    public Long rPush(String key, Object... values) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForList().rightPushAll(key, values),
            "Redis rPush operation failed for key: " + key
        );
    }

    @Override
    public <T> T lPop(String key, Class<T> clazz) {
        return executeWithExceptionHandling(() -> {
            Object value = redisTemplate.opsForList().leftPop(key);
            return convertValue(value, clazz);
        }, "Redis lPop operation failed for key: " + key);
    }

    @Override
    public <T> T rPop(String key, Class<T> clazz) {
        return executeWithExceptionHandling(() -> {
            Object value = redisTemplate.opsForList().rightPop(key);
            return convertValue(value, clazz);
        }, "Redis rPop operation failed for key: " + key);
    }

    @Override
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        return executeWithExceptionHandling(() -> {
            List<Object> values = redisTemplate.opsForList().range(key, start, end);
            List<T> result = new ArrayList<>();

            if (values != null) {
                for (Object value : values) {
                    result.add(convertValue(value, clazz));
                }
            }

            return result;
        }, "Redis lRange operation failed for key: " + key);
    }

    @Override
    public Long increment(String key) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForValue().increment(key),
            "Redis increment operation failed for key: " + key
        );
    }

    @Override
    public Long increment(String key, long delta) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForValue().increment(key, delta),
            "Redis increment operation failed for key: " + key + ", delta: " + delta
        );
    }

    @Override
    public Long decrement(String key) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForValue().decrement(key),
            "Redis decrement operation failed for key: " + key
        );
    }

    @Override
    public Long decrement(String key, long delta) {
        return executeWithExceptionHandling(() ->
            redisTemplate.opsForValue().decrement(key, delta),
            "Redis decrement operation failed for key: " + key + ", delta: " + delta
        );
    }

    /**
     * 统一的异常处理方法
     */
    private <T> T executeWithExceptionHandling(RedisOperation<T> operation, String errorMessage) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.error(errorMessage, e);
            throw new BusinessException(errorMessage, e);
        }
    }


    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> clazz) {
        if (Objects.isNull(value)) {
            return null;
        }

        // 类型匹配，直接返回
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        // 处理 CacheWrapper 包装的空值
        if (value instanceof CacheWrapper wrapper) {
            if (!wrapper.containsValue()) {
                return null;
            }

            // 如果包装的值类型匹配，直接返回
            if (clazz.isInstance(wrapper.getValue())) {
                return (T) wrapper.getValue();
            }

            // 类型不匹配，返回 null
            return null;
        }

        // 直接类型匹配
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        return null;
    }

    /**
     * Redis 操作的函数式接口
     */
    @FunctionalInterface
    private interface RedisOperation<T> {
        T execute() throws Exception;
    }
}