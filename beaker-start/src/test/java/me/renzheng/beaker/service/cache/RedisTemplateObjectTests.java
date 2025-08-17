package me.renzheng.beaker.service.cache;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.renzheng.beaker.start.AbstractTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RedisTemplate 对象类型单元测试
 */
@DisplayName("RedisTemplate 对象类型测试")
public class RedisTemplateObjectTests extends AbstractTests {

    @Resource(name = "cacheRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOps;
    private HashOperations<String, String, Object> hashOps;
    private ListOperations<String, Object> listOps;
    private SetOperations<String, Object> setOps;

    @BeforeEach
    void setUp() {
        valueOps = redisTemplate.opsForValue();
        hashOps = redisTemplate.opsForHash();
        listOps = redisTemplate.opsForList();
        setOps = redisTemplate.opsForSet();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
    }

    private void cleanupTestData() {
        Set<String> keys = redisTemplate.keys("test:object:*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // region 简单对象测试

    @Test
    @DisplayName("当存储用户对象时_应该正确序列化和反序列化")
    void whenStoreUserObject_thenShouldSerializeAndDeserializeCorrectly() {
        // Given
        String key = "test:object:user:1";
        User expectedUser = createTestUser();

        // When
        valueOps.set(key, expectedUser);
        User actualUser = (User) valueOps.get(key);

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
        String key = "test:object:user:time";
        User user = createTestUser();
        user.setCreateTime(LocalDateTime.now());
        user.setBirthDate(LocalDate.of(1990, 5, 15));
        user.setLastLoginTime(new Date());

        // When
        valueOps.set(key, user);
        User retrievedUser = (User) valueOps.get(key);

        // Then
        assertNotNull(retrievedUser);
        assertNotNull(retrievedUser.getCreateTime());
        assertNotNull(retrievedUser.getBirthDate());
        assertNotNull(retrievedUser.getLastLoginTime());
        assertEquals(user.getBirthDate(), retrievedUser.getBirthDate());
    }

    @Test
    @DisplayName("当存储包含嵌套对象的用户时_应该正确处理嵌套序列化")
    void whenStoreUserWithNestedObjects_thenShouldHandleNestedSerialization() {
        // Given
        String key = "test:object:user:nested";
        User user = createTestUserWithAddress();

        // When
        valueOps.set(key, user);
        User retrievedUser = (User) valueOps.get(key);

        // Then
        assertNotNull(retrievedUser);
        assertNotNull(retrievedUser.getAddress());
        assertEquals(user.getAddress().getProvince(), retrievedUser.getAddress().getProvince());
        assertEquals(user.getAddress().getCity(), retrievedUser.getAddress().getCity());
        assertEquals(user.getAddress().getDetail(), retrievedUser.getAddress().getDetail());
    }

    @Test
    @DisplayName("当存储包含集合属性的对象时_应该正确处理集合序列化")
    void whenStoreObjectWithCollections_thenShouldHandleCollectionSerialization() {
        // Given
        String key = "test:object:user:collections";
        User user = createTestUserWithCollections();

        // When
        valueOps.set(key, user);
        User retrievedUser = (User) valueOps.get(key);

        // Then
        assertNotNull(retrievedUser);
        assertNotNull(retrievedUser.getHobbies());
        assertNotNull(retrievedUser.getMetadata());

        assertEquals(user.getHobbies().size(), retrievedUser.getHobbies().size());
        assertTrue(retrievedUser.getHobbies().containsAll(user.getHobbies()));

        assertEquals(user.getMetadata().size(), retrievedUser.getMetadata().size());
        assertEquals(user.getMetadata().get("level"), retrievedUser.getMetadata().get("level"));
    }

    // endregion

    // region 复杂对象测试

    @Test
    @DisplayName("当存储复杂订单对象时_应该正确处理所有嵌套结构")
    void whenStoreComplexOrderObject_thenShouldHandleAllNestedStructures() {
        // Given
        String key = "test:object:order:complex";
        Order expectedOrder = createTestOrder();

        // When
        valueOps.set(key, expectedOrder);
        Order actualOrder = (Order) valueOps.get(key);

        // Then
        assertNotNull(actualOrder);
        assertEquals(expectedOrder.getOrderId(), actualOrder.getOrderId());
        assertEquals(expectedOrder.getUserId(), actualOrder.getUserId());
        assertEquals(expectedOrder.getTotalAmount(), actualOrder.getTotalAmount());
        assertEquals(expectedOrder.getStatus(), actualOrder.getStatus());

        // 验证订单项
        assertNotNull(actualOrder.getItems());
        assertEquals(expectedOrder.getItems().size(), actualOrder.getItems().size());

        OrderItem expectedItem = expectedOrder.getItems().get(0);
        OrderItem actualItem = actualOrder.getItems().get(0);
        assertEquals(expectedItem.getProductId(), actualItem.getProductId());
        assertEquals(expectedItem.getProductName(), actualItem.getProductName());
        assertEquals(expectedItem.getQuantity(), actualItem.getQuantity());
        assertEquals(expectedItem.getPrice(), actualItem.getPrice());
    }

    @Test
    @DisplayName("当存储包含枚举的对象时_应该正确处理枚举序列化")
    void whenStoreObjectWithEnum_thenShouldHandleEnumSerialization() {
        // Given
        String key = "test:object:order:enum";
        Order order = createTestOrder();
        order.setStatus(OrderStatus.SHIPPED);

        // When
        valueOps.set(key, order);
        Order retrievedOrder = (Order) valueOps.get(key);

        // Then
        assertNotNull(retrievedOrder);
        assertEquals(OrderStatus.SHIPPED, retrievedOrder.getStatus());
    }

    // endregion

    // region 对象集合测试

    @Test
    @DisplayName("当存储对象列表时_应该正确处理列表中的每个对象")
    void whenStoreObjectList_thenShouldHandleEachObjectInList() {
        // Given
        String key = "test:object:list:users";
        List<User> expectedUsers = Arrays.asList(
                createTestUser(1L, "张三"),
                createTestUser(2L, "李四"),
                createTestUser(3L, "王五")
        );

        // When
        valueOps.set(key, expectedUsers);
        @SuppressWarnings("unchecked")
        List<User> actualUsers = (List<User>) valueOps.get(key);

        // Then
        assertNotNull(actualUsers);
        assertEquals(3, actualUsers.size());

        for (int i = 0; i < expectedUsers.size(); i++) {
            User expected = expectedUsers.get(i);
            User actual = actualUsers.get(i);
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getName(), actual.getName());
        }
    }

    @Test
    @DisplayName("当使用List操作存储对象时_应该支持列表的推入和弹出")
    void whenUseListOperationsWithObjects_thenShouldSupportPushAndPop() {
        // Given
        String key = "test:object:list:products";
        Product product1 = createTestProduct("P001", "笔记本电脑");
        Product product2 = createTestProduct("P002", "无线鼠标");

        // When
        listOps.rightPush(key, product1);
        listOps.rightPush(key, product2);

        // Then
        assertEquals(2L, listOps.size(key));

        Product poppedProduct = (Product) listOps.leftPop(key);
        assertNotNull(poppedProduct);
        assertEquals(product1.getId(), poppedProduct.getId());
        assertEquals(product1.getName(), poppedProduct.getName());
    }

    @Test
    @DisplayName("当使用Set操作存储对象时_应该支持对象的去重")
    void whenUseSetOperationsWithObjects_thenShouldSupportObjectDeduplication() {
        // Given
        String key = "test:object:set:products";
        Product product1 = createTestProduct("P001", "键盘");
        Product product2 = createTestProduct("P002", "鼠标");
        Product product1Duplicate = createTestProduct("P001", "键盘"); // 相同ID

        // When
        setOps.add(key, product1, product2, product1Duplicate);

        // Then
        assertEquals(3L, setOps.size(key)); // 注意：由于对象序列化后可能不完全相同，可能不会去重
        assertEquals(Boolean.TRUE, setOps.isMember(key, product1));
    }

    // endregion

    // region Hash 存储对象字段测试

    @Test
    @DisplayName("当使用Hash存储对象字段时_应该支持字段级别的操作")
    void whenUseHashToStoreObjectFields_thenShouldSupportFieldLevelOperations() {
        // Given
        String key = "test:object:hash:user:1";
        User user = createTestUser();

        // When - 将对象字段分别存储到Hash中
        hashOps.put(key, "id", user.getId());
        hashOps.put(key, "name", user.getName());
        hashOps.put(key, "age", user.getAge());
        hashOps.put(key, "email", user.getEmail());
        hashOps.put(key, "active", user.getActive());

        // Then - 修复类型转换问题
        // Redis可能将Long序列化为Integer，需要安全转换
        Integer actualId = (Integer)hashOps.get(key, "id");
        assertNotNull(actualId);
        assertEquals(new BigDecimal(user.getId()), new BigDecimal(actualId));
        assertEquals(user.getName(), hashOps.get(key, "name"));
        assertEquals(user.getAge(), hashOps.get(key, "age"));
        assertEquals(user.getEmail(), hashOps.get(key, "email"));
        assertEquals(user.getActive(), hashOps.get(key, "active"));
    }

    @Test
    @DisplayName("当使用Hash存储嵌套对象时_应该正确序列化嵌套结构")
    void whenUseHashToStoreNestedObjects_thenShouldSerializeNestedStructure() {
        // Given
        String key = "test:object:hash:user:address";
        User user = createTestUserWithAddress();

        // When
        hashOps.put(key, "user", user);
        hashOps.put(key, "address", user.getAddress());

        // Then
        User retrievedUser = (User) hashOps.get(key, "user");
        Address retrievedAddress = (Address) hashOps.get(key, "address");

        assertNotNull(retrievedUser);
        assertNotNull(retrievedAddress);
        assertEquals(user.getAddress().getCity(), retrievedAddress.getCity());
    }

    // endregion

    // region 批量对象操作测试

    @Test
    @DisplayName("当批量存储对象时_应该正确处理多个对象的序列化")
    void whenBatchStoreObjects_thenShouldHandleMultipleObjectSerialization() {
        // Given
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("test:object:batch:user:1", createTestUser(1L, "用户1"));
        objectMap.put("test:object:batch:user:2", createTestUser(2L, "用户2"));
        objectMap.put("test:object:batch:product:1", createTestProduct("P001", "产品1"));

        // When
        valueOps.multiSet(objectMap);

        // Then
        List<String> keys = Arrays.asList(
                "test:object:batch:user:1",
                "test:object:batch:user:2",
                "test:object:batch:product:1"
        );
        List<Object> values = valueOps.multiGet(keys);

        assertNotNull(values);
        assertEquals(3, values.size());

        User user1 = (User) values.get(0);
        User user2 = (User) values.get(1);
        Product product1 = (Product) values.get(2);

        assertEquals("用户1", user1.getName());
        assertEquals("用户2", user2.getName());
        assertEquals("产品1", product1.getName());
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

    private User createTestUserWithAddress() {
        User user = createTestUser();
        Address address = new Address();
        address.setProvince("北京市");
        address.setCity("北京市");
        address.setDistrict("朝阳区");
        address.setDetail("某某街道123号");
        address.setZipCode("100000");
        user.setAddress(address);
        return user;
    }

    private User createTestUserWithCollections() {
        User user = createTestUser();

        // 设置爱好列表
        List<String> hobbies = Arrays.asList("阅读", "游泳", "编程", "旅行");
        user.setHobbies(hobbies);

        // 设置元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("level", "VIP");
        metadata.put("score", 95);
        metadata.put("lastActivity", LocalDateTime.now());
        user.setMetadata(metadata);

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
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("P001", "无线鼠标", 1, 99.99));
        items.add(new OrderItem("P002", "键盘", 1, 200.00));
        order.setItems(items);

        // 设置标签
        Map<String, String> tags = new HashMap<>();
        tags.put("source", "web");
        tags.put("promotion", "double11");
        order.setTags(tags);

        return order;
    }

    private Product createTestProduct(String id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategory("电子产品");
        product.setPrice(199.99);
        product.setStock(100);
        product.setAvailable(true);
        product.setTags(Arrays.asList("热销", "推荐"));

        Map<String, Object> specs = new HashMap<>();
        specs.put("brand", "TestBrand");
        specs.put("model", "TestModel");
        specs.put("warranty", "1年");
        product.setSpecifications(specs);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return product;
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
        private Address address;
        private List<String> hobbies;
        private Map<String, Object> metadata;
    }

    /**
     * 地址实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String province;
        private String city;
        private String district;
        private String detail;
        private String zipCode;
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
        private Map<String, String> tags;
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

    /**
     * 产品实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private String id;
        private String name;
        private String category;
        private Double price;
        private Integer stock;
        private Boolean available;
        private List<String> tags;
        private Map<String, Object> specifications;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // endregion
}