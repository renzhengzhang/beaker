package me.renzheng.beaker.service.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 *
 * @author <a href="mailto:renzheng.zh@gmail.com">Renzheng Zhang</a>
 */
public interface CacheService {

    // region Base Operation
    void set(String key, Object value);

    void set(String key, Object value, Duration timeout);

    void set(String key, Object value, long timeout, TimeUnit unit);

    <T> T get(String key, Class<T> clazz);

    String getString(String key);

    Boolean delete(String key);

    Long delete(String... keys);

    Boolean exists(String key);

    Boolean expire(String key, Duration timeout);

    // endregion

    // region Multi Operation
    <T> void multiSet(Map<String, T> keyValues);

    <T> void multiSet(Map<String, T> keyValues, Duration timeout);

    <T> List<T> multiGet(List<String> keys, Class<T> clazz);

    // endregion

    // region Hash Operation
    void hSet(String key, String field, Object value);

    <T> T hGet(String key, String field, Class<T> clazz);

    Map<String, Object> hGetAll(String key);

    Boolean hExists(String key, String field);

    Long hDelete(String key, String... fields);

    // endregion

    // region Set Operation

    Long sAdd(String key, Object... values);

    Set<Object> sMembers(String key);

    Boolean sIsMember(String key, Object value);

    Long sRemove(String key, Object... values);

    // endregion

    // region List Operation
    Long lPush(String key, Object... values);

    Long rPush(String key, Object... values);

    <T> T lPop(String key, Class<T> clazz);

    <T> T rPop(String key, Class<T> clazz);

    <T> List<T> lRange(String key, long start, long end, Class<T> clazz);

    // endregion

    // region Counter Operation

    Long increment(String key);

    Long increment(String key, long delta);

    Long decrement(String key);

    Long decrement(String key, long delta);

    // endregion
}
