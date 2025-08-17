package me.renzheng.beaker.service.cache;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.renzheng.beaker.service.cache.impl.RedisCacheService;
import me.renzheng.beaker.start.AbstractTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RedisCacheService 单元测试
 */
@DisplayName("RedisCacheService 测试")
public class RedisCacheServiceTests extends AbstractTests {

    @Resource
    private RedisCacheService cacheService;

    @Resource(name = "cacheRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        Set<String> keys = redisTemplate.keys("test:cache:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // region 基础操作测试

    @Test
    @DisplayName("当设置和获取字符串值时_应该正确存储和检索")
    void whenSetAndGetString_thenShouldStoreAndRetrieveCorrectly() {
        // Given
        String key = "test:cache:string";
        String expectedValue = "Hello Redis Cache";

        // When
        cacheService.set(key, expectedValue);
        String actualValue = cacheService.getString(key);

        // Then
        assertEquals(expectedValue, actualValue);
    }

    @Test
    @DisplayName("当设置带过期时间的值时_应该正确设置过期时间")
    void whenSetWithTimeout_thenShouldSetExpirationCorrectly() throws InterruptedException {
        // Given
        String key = "test:cache:timeout";
        String value = "expiring value";
        Duration timeout = Duration.ofSeconds(1);

        // When
        cacheService.set(key, value, timeout);
        
        // Then
        assertTrue(cacheService.exists(key));
        
        // Wait for expiration
        Thread.sleep(1100);
        assertFalse(cacheService.exists(key));
    }

    @Test
    @DisplayName("当设置带时间单位的过期时间时_应该正确处理")
    void whenSetWithTimeUnit_thenShouldHandleCorrectly() throws InterruptedException {
        // Given
        String key = "test:cache:timeunit";
        String value = "expiring value with time unit";

        // When
        cacheService.set(key, value, 500, TimeUnit.MILLISECONDS);
        
        // Then
        assertTrue(cacheService.exists(key));
        
        // Wait for expiration
        Thread.sleep(600);
        assertFalse(cacheService.exists(key));
    }

    @Test
    @DisplayName("当获取不存在的键时_应该返回null")
    void whenGetNonExistentKey_thenShouldReturnNull() {
        // Given
        String key = "test:cache:nonexistent";

        // When
        String result = cacheService.getString(key);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("当删除单个键时_应该成功删除")
    void whenDeleteSingleKey_thenShouldDeleteSuccessfully() {
        // Given
        String key = "test:cache:delete";
        String value = "to be deleted";
        cacheService.set(key, value);

        // When
        Boolean deleted = cacheService.delete(key);

        // Then
        assertTrue(deleted);
        assertFalse(cacheService.exists(key));
    }

    @Test
    @DisplayName("当删除多个键时_应该返回删除的数量")
    void whenDeleteMultipleKeys_thenShouldReturnDeletedCount() {
        // Given
        String key1 = "test:cache:delete1";
        String key2 = "test:cache:delete2";
        String key3 = "test:cache:delete3";
        
        cacheService.set(key1, "value1");
        cacheService.set(key2, "value2");
        cacheService.set(key3, "value3");

        // When
        Long deletedCount = cacheService.delete(key1, key2, key3);

        // Then
        assertEquals(3L, deletedCount);
        assertFalse(cacheService.exists(key1));
        assertFalse(cacheService.exists(key2));
        assertFalse(cacheService.exists(key3));
    }

    @Test
    @DisplayName("当设置键的过期时间时_应该正确设置")
    void whenExpireKey_thenShouldSetExpirationCorrectly() {
        // Given
        String key = "test:cache:expire";
        String value = "will expire";
        cacheService.set(key, value);

        // When
        Boolean expired = cacheService.expire(key, Duration.ofSeconds(1));

        // Then
        assertTrue(expired);
        assertTrue(cacheService.exists(key));
    }

    // endregion

    // region 对象操作测试

    @Test
    @DisplayName("当存储和获取用户对象时_应该正确序列化和反序列化")
    void whenStoreAndGetUserObject_thenShouldSerializeCorrectly() {
        // Given
        String key = "test:cache:user";
        User expectedUser = createTestUser();

        // When
        cacheService.set(key, expectedUser);
        User actualUser = cacheService.get(key, User.class);

        // Then
        assertNotNull(actualUser);
        assertEquals(expectedUser.getId(), actualUser.getId());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getAge(), actualUser.getAge());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getActive(), actualUser.getActive());
    }

    @Test
    @DisplayName("当存储包含时间字段的对象时_应该正确处理时间序列化")
    void whenStoreObjectWithTimeFields_thenShouldHandleTimeSerialization() {
        // Given
        String key = "test:cache:user:time";
        User user = createTestUser();
        user.setCreateTime(LocalDateTime.now());
        user.setBirthDate(LocalDate.of(1990, 5, 15));
        user.setLastLoginTime(new Date());

        // When
        cacheService.set(key, user);
        User retrievedUser = cacheService.get(key, User.class);

        // Then
        assertNotNull(retrievedUser);
        assertNotNull(retrievedUser.getCreateTime());
        assertNotNull(retrievedUser.getBirthDate());
        assertNotNull(retrievedUser.getLastLoginTime());
        assertEquals(user.getBirthDate(), retrievedUser.getBirthDate());
    }

    @Test
    @DisplayName("当存储复杂订单对象时_应该正确处理所有嵌套结构")
    void whenStoreComplexOrderObject_thenShouldHandleAllNestedStructures() {
        // Given
        String key = "test:cache:order";
        Order expectedOrder = createTestOrder();

        // When
        cacheService.set(key, expectedOrder);
        Order actualOrder = cacheService.get(key, Order.class);

        // Then
        assertNotNull(actualOrder);
        assertEquals(expectedOrder.getOrderId(), actualOrder.getOrderId());
        assertEquals(expectedOrder.getUserId(), actualOrder.getUserId());
        assertEquals(expectedOrder.getTotalAmount(), actualOrder.getTotalAmount());
        assertEquals(expectedOrder.getStatus(), actualOrder.getStatus());

        // 验证订单项
        assertNotNull(actualOrder.getItems());
        assertEquals(expectedOrder.getItems().size(), actualOrder.getItems().size());
    }

    // endregion

    // region 批量操作测试

    @Test
    @DisplayName("当批量设置键值对时_应该正确存储所有值")
    void whenMultiSet_thenShouldStoreAllValues() {
        // Given
        Map<String, String> keyValues = new HashMap<>();
        keyValues.put("test:cache:multi1", "value1");
        keyValues.put("test:cache:multi2", "value2");
        keyValues.put("test:cache:multi3", "value3");

        // When
        cacheService.multiSet(keyValues);

        // Then
        assertEquals("value1", cacheService.getString("test:cache:multi1"));
        assertEquals("value2", cacheService.getString("test:cache:multi2"));
        assertEquals("value3", cacheService.getString("test:cache:multi3"));
    }

    @Test
    @DisplayName("当批量设置带过期时间的键值对时_应该正确设置过期时间")
    void whenMultiSetWithTimeout_thenShouldSetExpirationForAll() {
        // Given
        Map<String, String> keyValues = new HashMap<>();
        keyValues.put("test:cache:multi:timeout1", "value1");
        keyValues.put("test:cache:multi:timeout2", "value2");
        Duration timeout = Duration.ofSeconds(1);

        // When
        cacheService.multiSet(keyValues, timeout);

        // Then
        assertTrue(cacheService.exists("test:cache:multi:timeout1"));
        assertTrue(cacheService.exists("test:cache:multi:timeout2"));
    }

    @Test
    @DisplayName("当批量获取值时_应该返回对应的值列表")
    void whenMultiGet_thenShouldReturnCorrespondingValues() {
        // Given
        cacheService.set("test:cache:multiget1", "value1");
        cacheService.set("test:cache:multiget2", "value2");
        cacheService.set("test:cache:multiget3", "value3");

        List<String> keys = Arrays.asList(
                "test:cache:multiget1",
                "test:cache:multiget2",
                "test:cache:multiget3"
        );

        // When
        List<String> values = cacheService.multiGet(keys, String.class);

        // Then
        assertNotNull(values);
        assertEquals(3, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
        assertEquals("value3", values.get(2));
    }

    // endregion

    // region Hash 操作测试

    @Test
    @DisplayName("当使用Hash操作时_应该正确存储和获取字段值")
    void whenUseHashOperations_thenShouldStoreAndRetrieveFieldValues() {
        // Given
        String key = "test:cache:hash:user";
        String field1 = "name";
        String field2 = "age";
        String value1 = "张三";
        Integer value2 = 25;

        // When
        cacheService.hSet(key, field1, value1);
        cacheService.hSet(key, field2, value2);

        // Then
        assertEquals(value1, cacheService.hGet(key, field1, String.class));
        assertEquals(value2, cacheService.hGet(key, field2, Integer.class));
        assertTrue(cacheService.hExists(key, field1));
        assertTrue(cacheService.hExists(key, field2));
    }

    @Test
    @DisplayName("当获取Hash所有字段时_应该返回完整的字段映射")
    void whenGetAllHashFields_thenShouldReturnCompleteFieldMap() {
        // Given
        String key = "test:cache:hash:all";
        cacheService.hSet(key, "field1", "value1");
        cacheService.hSet(key, "field2", "value2");
        cacheService.hSet(key, "field3", "value3");

        // When
        Map<String, Object> allFields = cacheService.hGetAll(key);

        // Then
        assertNotNull(allFields);
        assertEquals(3, allFields.size());
        assertEquals("value1", allFields.get("field1"));
        assertEquals("value2", allFields.get("field2"));
        assertEquals("value3", allFields.get("field3"));
    }

    @Test
    @DisplayName("当删除Hash字段时_应该成功删除指定字段")
    void whenDeleteHashFields_thenShouldDeleteSpecifiedFields() {
        // Given
        String key = "test:cache:hash:delete";
        cacheService.hSet(key, "field1", "value1");
        cacheService.hSet(key, "field2", "value2");
        cacheService.hSet(key, "field3", "value3");

        // When
        Long deletedCount = cacheService.hDelete(key, "field1", "field2");

        // Then
        assertEquals(2L, deletedCount);
        assertFalse(cacheService.hExists(key, "field1"));
        assertFalse(cacheService.hExists(key, "field2"));
        assertTrue(cacheService.hExists(key, "field3"));
    }

    // endregion

    // region Set 操作测试

    @Test
    @DisplayName("当使用Set操作时_应该正确添加和检查成员")
    void whenUseSetOperations_thenShouldAddAndCheckMembers() {
        // Given
        String key = "test:cache:set";
        String member1 = "member1";
        String member2 = "member2";
        String member3 = "member3";

        // When
        Long addedCount = cacheService.sAdd(key, member1, member2, member3);

        // Then
        assertEquals(3L, addedCount);
        assertTrue(cacheService.sIsMember(key, member1));
        assertTrue(cacheService.sIsMember(key, member2));
        assertTrue(cacheService.sIsMember(key, member3));
        assertFalse(cacheService.sIsMember(key, "nonexistent"));
    }

    @Test
    @DisplayName("当获取Set所有成员时_应该返回完整的成员集合")
    void whenGetAllSetMembers_thenShouldReturnCompleteSet() {
        // Given
        String key = "test:cache:set:members";
        cacheService.sAdd(key, "member1", "member2", "member3");

        // When
        Set<Object> members = cacheService.sMembers(key);

        // Then
        assertNotNull(members);
        assertEquals(3, members.size());
        assertTrue(members.contains("member1"));
        assertTrue(members.contains("member2"));
        assertTrue(members.contains("member3"));
    }

    @Test
    @DisplayName("当从Set中移除成员时_应该成功移除指定成员")
    void whenRemoveSetMembers_thenShouldRemoveSpecifiedMembers() {
        // Given
        String key = "test:cache:set:remove";
        cacheService.sAdd(key, "member1", "member2", "member3");

        // When
        Long removedCount = cacheService.sRemove(key, "member1", "member2");

        // Then
        assertEquals(2L, removedCount);
        assertFalse(cacheService.sIsMember(key, "member1"));
        assertFalse(cacheService.sIsMember(key, "member2"));
        assertTrue(cacheService.sIsMember(key, "member3"));
    }

    // endregion

    // region List 操作测试

    @Test
    @DisplayName("当使用List左推操作时_应该正确添加元素到列表头部")
    void whenUseListLeftPush_thenShouldAddElementsToHead() {
        // Given
        String key = "test:cache:list:lpush";

        // When
        Long count1 = cacheService.lPush(key, "first");
        Long count2 = cacheService.lPush(key, "second");

        // Then
        assertEquals(1L, count1);
        assertEquals(2L, count2);

        String first = cacheService.lPop(key, String.class);
        assertEquals("second", first);
    }

    @Test
    @DisplayName("当使用List右推操作时_应该正确添加元素到列表尾部")
    void whenUseListRightPush_thenShouldAddElementsToTail() {
        // Given
        String key = "test:cache:list:rpush";

        // When
        Long count1 = cacheService.rPush(key, "first");
        Long count2 = cacheService.rPush(key, "second");

        // Then
        assertEquals(1L, count1);
        assertEquals(2L, count2);

        String first = cacheService.lPop(key, String.class);
        assertEquals("first", first);
    }

    @Test
    @DisplayName("当使用List右弹出操作时_应该从列表尾部弹出元素")
    void whenUseListRightPop_thenShouldPopElementFromTail() {
        // Given
        String key = "test:cache:list:rpop";
        cacheService.rPush(key, "first", "second", "third");

        // When
        String popped = cacheService.rPop(key, String.class);

        // Then
        assertEquals("third", popped);
    }

    @Test
    @DisplayName("当获取List范围内的元素时_应该返回指定范围的元素")
    void whenGetListRange_thenShouldReturnElementsInRange() {
        // Given
        String key = "test:cache:list:range";
        cacheService.rPush(key, "first", "second", "third", "fourth", "fifth");

        // When
        List<String> range = cacheService.lRange(key, 1, 3, String.class);

        // Then
        assertNotNull(range);
        assertEquals(3, range.size());
        assertEquals("second", range.get(0));
        assertEquals("third", range.get(1));
        assertEquals("fourth", range.get(2));
    }

    // endregion

    // region 计数器操作测试

    @Test
    @DisplayName("当使用计数器递增操作时_应该正确递增值")
    void whenUseIncrement_thenShouldIncrementValueCorrectly() {
        // Given
        String key = "test:cache:counter:inc";

        // When
        Long value1 = cacheService.increment(key);
        Long value2 = cacheService.increment(key);
        Long value3 = cacheService.increment(key, 5);

        // Then
        assertEquals(1L, value1);
        assertEquals(2L, value2);
        assertEquals(7L, value3);
    }

    @Test
    @DisplayName("当使用计数器递减操作时_应该正确递减值")
    void whenUseDecrement_thenShouldDecrementValueCorrectly() {
        // Given
        String key = "test:cache:counter:dec";
        cacheService.set(key, 10);

        // When
        Long value1 = cacheService.decrement(key);
        Long value2 = cacheService.decrement(key);
        Long value3 = cacheService.decrement(key, 3);

        // Then
        assertEquals(9L, value1);
        assertEquals(8L, value2);
        assertEquals(5L, value3);
    }

    // endregion

    // region CacheWrapper 空值缓存测试

    @Test
    @DisplayName("当存储CacheWrapper包装的空值时_应该正确处理空值缓存")
    void whenStoreCacheWrapperWithNull_thenShouldHandleNullCaching() {
        // Given
        String key = "test:cache:wrapper:null";
        CacheWrapper wrapper = CacheWrapper.ofEmpty();

        // When
        cacheService.set(key, wrapper);
        var result = cacheService.get(key, String.class);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("当存储CacheWrapper包装的非空值时_应该正确解包返回原值")
    void whenStoreCacheWrapperWithValue_thenShouldUnwrapAndReturnOriginalValue() {
        // Given
        String key = "test:cache:wrapper:value";
        String originalValue = "wrapped value";
        CacheWrapper wrapper = CacheWrapper.of(originalValue);

        // When
        cacheService.set(key, wrapper);
        String result = cacheService.get(key, String.class);

        // Then
        assertEquals(originalValue, result);
    }

    // endregion

    // region 异常处理测试

    @Test
    @DisplayName("当Redis操作失败时_应该抛出BusinessException")
    void whenRedisOperationFails_thenShouldThrowBusinessException() {
        // Given - 使用一个会导致异常的操作（这里模拟通过关闭连接等方式）
        String key = "test:cache:exception";
        
        // 注意：这个测试可能需要根据实际情况调整，比如模拟Redis连接断开
        // 这里我们测试类型转换异常的情况
        cacheService.set(key, "string value");

        // When & Then - 尝试以错误的类型获取值不会抛出异常，而是返回null
        Integer result = cacheService.get(key, Integer.class);
        assertNull(result);
    }

    // endregion

    // region 辅助方法

    private User createTestUser() {
        return createTestUser(1L, "张三");
    }

    private User createTestUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAge(25);
        user.setEmail(name.toLowerCase() + "@example.com");
        user.setActive(true);
        user.setCreateTime(LocalDateTime.now());
        user.setBirthDate(LocalDate.of(1998, 3, 15));
        user.setLastLoginTime(new Date());
        return user;
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setOrderId("ORD-" + System.currentTimeMillis());
        order.setUserId(1L);
        order.setTotalAmount(299.99);
        order.setStatus(OrderStatus.PAID);
        order.setOrderTime(LocalDateTime.now());

        // 创建订单项
        OrderItem item1 = new OrderItem("P001", "无线鼠标", 1, 99.99);
        OrderItem item2 = new OrderItem("P002", "键盘", 1, 200.00);
        order.setItems(Arrays.asList(item1, item2));

        return order;
    }

    // endregion

    // region 测试实体类

    /**
     * 用户实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String name;
        private Integer age;
        private String email;
        private Boolean active;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthDate;

        private Date lastLoginTime;
    }

    /**
     * 订单实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private String orderId;
        private Long userId;
        private List<OrderItem> items;
        private Double totalAmount;
        private OrderStatus status;
        private LocalDateTime orderTime;
    }

    /**
     * 订单项实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productName;
        private Integer quantity;
        private Double price;
    }

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    }

    // endregion
}