package me.renzheng.beaker.service.cache;


import jakarta.annotation.Resource;
import me.renzheng.beaker.start.AbstractTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RedisTemplate 集成测试类，用于测试 Redis 的各种数据类型操作
 *
 * @author Renzheng Zhang
 */
@DisplayName("RedisTemplate 测试")
public class RedisTemplateTests extends AbstractTests {

    @Resource(name = "cacheRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOps;
    private HashOperations<String, String, Object> hashOps;
    private ListOperations<String, Object> listOps;
    private SetOperations<String, Object> setOps;
    private ZSetOperations<String, Object> zSetOps;

    @BeforeEach
    void setUp() {
        valueOps = redisTemplate.opsForValue();
        hashOps = redisTemplate.opsForHash();
        listOps = redisTemplate.opsForList();
        setOps = redisTemplate.opsForSet();
        zSetOps = redisTemplate.opsForZSet();

        cleanupTestData();
    }

    private void cleanupTestData() {
        Set<String> keys = redisTemplate.keys("test:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // region String 类型测试

    @Test
    @DisplayName("当设置字符串值时_应该能够正确获取")
    void whenSetStringValue_thenShouldRetrieveCorrectly() {
        // Given
        String key = "test:string:key";
        String expectedValue = "Hello Redis";

        // When
        valueOps.set(key, expectedValue);
        String actualValue = (String) valueOps.get(key);

        // Then
        assertEquals(expectedValue, actualValue);
    }

    @Test
    @DisplayName("当设置带过期时间的字符串值时_应该正确设置 TTL")
    void whenSetStringValueWithExpiration_thenShouldSetTTLCorrectly() {
        // Given
        String key = "test:string:expire";
        String value = "expire value";
        int expireSeconds = 5;

        // When
        valueOps.set(key, value, expireSeconds, TimeUnit.SECONDS);

        // Then
        assertTrue(redisTemplate.hasKey(key));
        long ttl = redisTemplate.getExpire(key);
        assertTrue(ttl > 0 && ttl <= expireSeconds);
    }

    @Test
    @DisplayName("当对数字字符串执行递增操作时_应该正确增加数值")
    void whenIncrementNumericString_thenShouldIncreaseValueCorrectly() {
        // Given
        String key = "test:string:counter";

        // When & Then
        Long result1 = valueOps.increment(key);
        assertEquals(1L, result1);

        Long result2 = valueOps.increment(key, 5);
        assertEquals(6L, result2);
    }

    @Test
    @DisplayName("当对数字字符串执行递减操作时_应该正确减少数值")
    void whenDecrementNumericString_thenShouldDecreaseValueCorrectly() {
        // Given
        String key = "test:string:counter";
        valueOps.set(key, 10);

        // When & Then
        Long result1 = valueOps.decrement(key);
        assertEquals(9L, result1);

        Long result2 = valueOps.decrement(key, 3);
        assertEquals(6L, result2);
    }

    @Test
    @DisplayName("当键不存在时使用SetIfAbsent_应该设置成功")
    void whenKeyNotExistsAndUseSetIfAbsent_thenShouldSetSuccessfully() {
        // Given
        String key = "test:string:setnx";
        String value = "first value";

        // When
        Boolean result = valueOps.setIfAbsent(key, value);

        // Then
        assertEquals(Boolean.TRUE, result);
        assertEquals(value, valueOps.get(key));
    }

    @Test
    @DisplayName("当键已存在时使用SetIfAbsent_应该设置失败")
    void whenKeyExistsAndUseSetIfAbsent_thenShouldFailToSet() {
        // Given
        String key = "test:string:setnx";
        String originalValue = "original value";
        String newValue = "new value";
        valueOps.set(key, originalValue);

        // When
        Boolean result = valueOps.setIfAbsent(key, newValue);

        // Then
        assertEquals(Boolean.FALSE, result);
        assertEquals(originalValue, valueOps.get(key));
    }

    // endregion

    // region Hash 类型测试

    @Test
    @DisplayName("当设置Hash字段时_应该能够正确获取单个字段")
    void whenSetHashFields_thenShouldRetrieveSingleFieldCorrectly() {
        // Given
        String key = "test:hash:user";
        String nameField = "name";
        String expectedName = "张三";

        // When
        hashOps.put(key, nameField, expectedName);
        String actualName = (String) hashOps.get(key, nameField);

        // Then
        assertEquals(expectedName, actualName);
    }

    @Test
    @DisplayName("当设置多个Hash字段时_应该能够获取所有字段")
    void whenSetMultipleHashFields_thenShouldRetrieveAllFields() {
        // Given
        String key = "test:hash:user";
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", "张三");
        userInfo.put("age", 25);
        userInfo.put("email", "zhangsan@example.com");

        // When
        hashOps.putAll(key, userInfo);
        Map<String, Object> retrievedInfo = hashOps.entries(key);

        // Then
        assertEquals(3, retrievedInfo.size());
        assertEquals("张三", retrievedInfo.get("name"));
        assertEquals(25, retrievedInfo.get("age"));
        assertEquals("zhangsan@example.com", retrievedInfo.get("email"));
    }

    @Test
    @DisplayName("当对Hash字段执行递增操作时_应该正确增加数值")
    void whenIncrementHashField_thenShouldIncreaseValueCorrectly() {
        // Given
        String key = "test:hash:stats";
        String field = "views";

        // When & Then
        Long result1 = hashOps.increment(key, field, 1);
        assertEquals(1L, result1);

        Long result2 = hashOps.increment(key, field, 10);
        assertEquals(11L, result2);
    }

    @Test
    @DisplayName("当检查Hash字段是否存在时_应该返回正确结果")
    void whenCheckHashFieldExists_thenShouldReturnCorrectResult() {
        // Given
        String key = "test:hash:exists";
        String existingField = "field1";
        String nonExistingField = "field2";

        // When
        hashOps.put(key, existingField, "value1");

        // Then
        assertTrue(hashOps.hasKey(key, existingField));
        assertFalse(hashOps.hasKey(key, nonExistingField));
    }

    // endregion

    // region List 类型测试

    @Test
    @DisplayName("当向列表左侧推入元素时_应该按LIFO顺序排列")
    void whenLeftPushToList_thenShouldArrangeInLIFOOrder() {
        // Given
        String key = "test:list:stack";
        String[] items = {"item1", "item2", "item3"};

        // When
        for (String item : items) {
            listOps.leftPush(key, item);
        }

        // Then
        List<Object> result = listOps.range(key, 0, -1);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("item3", result.get(0)); // 最后推入的在最前面
        assertEquals("item2", result.get(1));
        assertEquals("item1", result.get(2));
    }

    @Test
    @DisplayName("当向列表右侧推入元素时_应该按FIFO顺序排列")
    void whenRightPushToList_thenShouldArrangeInFIFOOrder() {
        // Given
        String key = "test:list:queue";
        String[] items = {"item1", "item2", "item3"};

        // When
        for (String item : items) {
            listOps.rightPush(key, item);
        }

        // Then
        List<Object> result = listOps.range(key, 0, -1);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));
    }

    @Test
    @DisplayName("当从列表弹出元素时_应该正确移除并返回元素")
    void whenPopFromList_thenShouldRemoveAndReturnElement() {
        // Given
        String key = "test:list:pop";
        listOps.rightPush(key, "first");
        listOps.rightPush(key, "second");
        listOps.rightPush(key, "third");

        // When
        Object leftPop = listOps.leftPop(key);
        Object rightPop = listOps.rightPop(key);

        // Then
        assertEquals("first", leftPop);
        assertEquals("third", rightPop);
        assertEquals(1L, listOps.size(key));
        assertEquals("second", listOps.index(key, 0));
    }

    @Test
    @DisplayName("当按索引访问列表元素时_应该返回正确元素")
    void whenAccessListByIndex_thenShouldReturnCorrectElement() {
        // Given
        String key = "test:list:index";
        String[] items = {"a", "b", "c"};
        for (String item : items) {
            listOps.rightPush(key, item);
        }

        // When & Then
        assertEquals("a", listOps.index(key, 0));
        assertEquals("b", listOps.index(key, 1));
        assertEquals("c", listOps.index(key, 2));
    }

    // endregion

    // region Set 类型测试

    @Test
    @DisplayName("当向集合添加元素时_应该去重并正确存储")
    void whenAddElementsToSet_thenShouldDeduplicateAndStoreCorrectly() {
        // Given
        String key = "test:set:tags";
        String[] tags = {"java", "spring", "redis", "java"}; // java重复

        // When
        setOps.add(key, tags);

        // Then
        assertEquals(3L, setOps.size(key)); // 去重后只有3个
        assertEquals(Boolean.TRUE, setOps.isMember(key, "java"));
        assertEquals(Boolean.TRUE, setOps.isMember(key, "spring"));
        assertEquals(Boolean.TRUE, setOps.isMember(key, "redis"));
    }

    @Test
    @DisplayName("当检查集合成员时_应该返回正确的存在状态")
    void whenCheckSetMembership_thenShouldReturnCorrectExistence() {
        // Given
        String key = "test:set:members";
        setOps.add(key, "member1", "member2");

        // When & Then
        assertEquals(Boolean.TRUE, setOps.isMember(key, "member1"));
        assertEquals(Boolean.TRUE, setOps.isMember(key, "member2"));
        assertNotEquals(Boolean.TRUE, setOps.isMember(key, "member3"));
    }

    @Test
    @DisplayName("当计算集合交集时_应该返回共同元素")
    void whenCalculateSetIntersection_thenShouldReturnCommonElements() {
        // Given
        String key1 = "test:set:skills1";
        String key2 = "test:set:skills2";
        setOps.add(key1, "java", "python", "javascript");
        setOps.add(key2, "java", "go", "javascript");

        // When
        Set<Object> intersection = setOps.intersect(key1, key2);

        // Then
        assertNotNull(intersection);
        assertEquals(2, intersection.size());
        assertTrue(intersection.contains("java"));
        assertTrue(intersection.contains("javascript"));
    }

    @Test
    @DisplayName("当计算集合并集时_应该返回所有不重复元素")
    void whenCalculateSetUnion_thenShouldReturnAllUniqueElements() {
        // Given
        String key1 = "test:set:group1";
        String key2 = "test:set:group2";
        setOps.add(key1, "a", "b", "c");
        setOps.add(key2, "c", "d", "e");

        // When
        Set<Object> union = setOps.union(key1, key2);

        // Then
        assertNotNull(union);
        assertEquals(5, union.size());
        assertTrue(union.containsAll(Arrays.asList("a", "b", "c", "d", "e")));
    }

    // endregion

    // region ZSet 类型测试

    @Test
    @DisplayName("当向有序集合添加带分数的元素时_应该按分数排序")
    void whenAddScoredElementsToZSet_thenShouldSortByScore() {
        // Given
        String key = "test:zset:leaderboard";

        // When
        zSetOps.add(key, "player1", 100);
        zSetOps.add(key, "player2", 200);
        zSetOps.add(key, "player3", 150);

        // Then
        assertEquals(3L, zSetOps.size(key));

        // 按分数从高到低获取
        Set<Object> topPlayers = zSetOps.reverseRange(key, 0, -1);
        assertNotNull(topPlayers);
        List<Object> playerList = new ArrayList<>(topPlayers);
        assertEquals("player2", playerList.get(0)); // 分数最高
        assertEquals("player3", playerList.get(1));
        assertEquals("player1", playerList.get(2)); // 分数最低
    }

    @Test
    @DisplayName("当获取有序集合元素分数时_应该返回正确分数")
    void whenGetZSetElementScore_thenShouldReturnCorrectScore() {
        // Given
        String key = "test:zset:scores";
        String member = "student1";
        double expectedScore = 95.5;

        // When
        zSetOps.add(key, member, expectedScore);
        Double actualScore = zSetOps.score(key, member);

        // Then
        assertEquals(expectedScore, actualScore);
    }

    @Test
    @DisplayName("当增加有序集合元素分数时_应该正确更新分数")
    void whenIncrementZSetElementScore_thenShouldUpdateScoreCorrectly() {
        // Given
        String key = "test:zset:increment";
        String member = "player";
        double initialScore = 100.0;
        double increment = 25.5;

        // When
        zSetOps.add(key, member, initialScore);
        Double newScore = zSetOps.incrementScore(key, member, increment);

        // Then
        assertEquals(125.5, newScore);
        assertEquals(125.5, zSetOps.score(key, member));
    }

    @Test
    @DisplayName("当获取有序集合元素排名时_应该返回正确排名")
    void whenGetZSetElementRank_thenShouldReturnCorrectRank() {
        // Given
        String key = "test:zset:ranking";
        zSetOps.add(key, "user1", 85.5);
        zSetOps.add(key, "user2", 92.0);
        zSetOps.add(key, "user3", 78.5);
        zSetOps.add(key, "user4", 95.5);

        // When
        Long user4Rank = zSetOps.reverseRank(key, "user4"); // 最高分
        Long user2Rank = zSetOps.reverseRank(key, "user2"); // 第二高分

        // Then
        // 排名从0开始
        assertEquals(0L, user4Rank);
        assertEquals(1L, user2Rank);
    }

    // endregion

    // region 通用操作测试

    @Test
    @DisplayName("当检查键是否存在时_应该返回正确状态")
    void whenCheckKeyExists_thenShouldReturnCorrectStatus() {
        // Given
        String existingKey = "test:key:existing";
        String nonExistingKey = "test:key:nonexisting";

        // When
        valueOps.set(existingKey, "value");

        // Then
        assertTrue(redisTemplate.hasKey(existingKey));
        assertFalse(redisTemplate.hasKey(nonExistingKey));
    }

    @Test
    @DisplayName("当设置键的过期时间时_应该正确设置TTL")
    void whenSetKeyExpiration_thenShouldSetTTLCorrectly() {
        // Given
        String key = "test:key:expire";
        valueOps.set(key, "value");

        // When
        redisTemplate.expire(key, Duration.ofSeconds(30));

        // Then
        long ttl = redisTemplate.getExpire(key);
        assertTrue(ttl > 0 && ttl <= 30);
    }

    @Test
    @DisplayName("当删除键时_应该成功删除并返回true")
    void whenDeleteKey_thenShouldDeleteSuccessfullyAndReturnTrue() {
        // Given
        String key = "test:key:delete";
        valueOps.set(key, "value");
        assertTrue(redisTemplate.hasKey(key));

        // When
        Boolean deleted = redisTemplate.delete(key);

        // Then
        assertTrue(deleted);
        assertFalse(redisTemplate.hasKey(key));
    }

    @Test
    @DisplayName("当批量设置键值时_应该正确设置所有键值对")
    void whenBatchSetKeyValues_thenShouldSetAllKeyValuePairs() {
        // Given
        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("test:batch:key1", "value1");
        keyValues.put("test:batch:key2", "value2");
        keyValues.put("test:batch:key3", "value3");

        // When
        valueOps.multiSet(keyValues);

        // Then
        List<String> keys = Arrays.asList("test:batch:key1", "test:batch:key2", "test:batch:key3");
        List<Object> values = valueOps.multiGet(keys);

        assertNotNull(values);
        assertEquals(3, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
        assertEquals("value3", values.get(2));
    }

    // endregion
}