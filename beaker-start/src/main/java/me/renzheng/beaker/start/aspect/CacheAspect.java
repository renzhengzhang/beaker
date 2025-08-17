package me.renzheng.beaker.start.aspect;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.renzheng.beaker.service.cache.CacheEvict;
import me.renzheng.beaker.service.cache.CachePut;
import me.renzheng.beaker.service.cache.CacheService;
import me.renzheng.beaker.service.cache.CacheWrapper;
import me.renzheng.beaker.service.cache.Cacheable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Aspect
@Component
public class CacheAspect {

    @Resource
    private CacheService cacheService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String key = generateKey(joinPoint, cacheable.value(), cacheable.key());
        long startTime = System.currentTimeMillis();

        // 检查条件
        if (!evaluateCondition(joinPoint, cacheable.condition())) {
            return joinPoint.proceed();
        }

        // 尝试从缓存获取
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        try {
            CacheWrapper cacheWrapper = cacheService.get(key, CacheWrapper.class);
            if (Objects.nonNull(cacheWrapper)) {
                if (cacheWrapper.containsValue() && returnType.isInstance(cacheWrapper.getValue())) {
                    // 缓存命中
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Cache hit for key: {}, duration: {}ms", key, duration);
                    return cacheWrapper.getValue();
                } else {
                    // 缓存了空值，直接返回 null
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Cache hit (null value) for key: {}, duration: {}ms", key, duration);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Cache get operation failed for key: {}, fallback to method execution", key, e);
        }

        // 缓存未命中或异常，执行方法
        log.info("Cache miss for key: {}", key);
        Object result = joinPoint.proceed();

        // 缓存结果
        if (Objects.nonNull(result) || cacheable.cacheNull()) {
            try {
                CacheWrapper cacheWrapper = CacheWrapper.of(result);

                if (cacheable.timeout() > 0) {
                    cacheService.set(key, cacheWrapper, cacheable.timeout(), cacheable.timeUnit());
                } else {
                    cacheService.set(key, cacheWrapper);
                }

                long duration = System.currentTimeMillis() - startTime;
                log.info("Cache stored for key: {}, duration: {}ms", key, duration);
            } catch (Exception e) {
                log.error("Cache set operation failed for key: {}", key, e);
            }
        }

        return result;
    }

    @Around("@annotation(cacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        // 检查条件
        if (!evaluateCondition(joinPoint, cacheEvict.condition())) {
            return joinPoint.proceed();
        }

        // 方法执行前清除缓存
        if (cacheEvict.beforeInvocation()) {
            evictCache(joinPoint, cacheEvict);
        }

        // 执行方法
        Object result = joinPoint.proceed();

        // 方法执行后清除缓存
        if (!cacheEvict.beforeInvocation()) {
            evictCache(joinPoint, cacheEvict);
        }

        return result;
    }

    @Around("@annotation(cachePut)")
    public Object handleCachePut(ProceedingJoinPoint joinPoint, CachePut cachePut) throws Throwable {
        // 检查条件
        if (!evaluateCondition(joinPoint, cachePut.condition())) {
            return joinPoint.proceed();
        }

        // 执行方法
        Object result = joinPoint.proceed();

        // 更新缓存
        if (Objects.nonNull(result) || cachePut.cacheNull()) {
            CacheWrapper cacheWrapper = CacheWrapper.of(result);
            String key = generateKey(joinPoint, cachePut.value(), cachePut.key());
            if (cachePut.timeout() > 0) {
                cacheService.set(key, cacheWrapper, cachePut.timeout(), cachePut.timeUnit());
            } else {
                cacheService.set(key, cacheWrapper);
            }
        }

        return result;
    }

    private void evictCache(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) {
        if (cacheEvict.allEntries()) {
            // 清除所有缓存
            String pattern = cacheEvict.value() + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (CollectionUtils.isNotEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        } else {
            // 清除指定缓存
            String key = generateKey(joinPoint, cacheEvict.value(), cacheEvict.key());
            cacheService.delete(key);
        }
    }

    private String generateKey(ProceedingJoinPoint joinPoint, String cacheName, String keyExpression) {
        String key;

        if (StringUtils.isNotBlank(keyExpression)) {
            // 使用 SpEL 表达式生成 key
            key = parseExpression(joinPoint, keyExpression);
        } else {
            // 默认 key 生成策略，全限定类名.方法名:参数名:参数值[,参数名:参数值]
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(signature.getDeclaringTypeName())
                    .append(".")
                    .append(signature.getName());

            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                keyBuilder.append(":");
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        keyBuilder.append(",");
                    }
                    keyBuilder.append(args[i] != null ? args[i].toString() : "null");
                }
            }
            key = keyBuilder.toString();
        }

        // 添加缓存名称前缀
        if (StringUtils.isNotBlank(cacheName)) {
            return cacheName + ":" + key;
        }

        return key;
    }

    @SuppressWarnings("Duplicates")
    private String parseExpression(ProceedingJoinPoint joinPoint, String expression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = discoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        Expression exp = parser.parseExpression(expression);
        Object value = exp.getValue(context);
        return value != null ? value.toString() : "";
    }

    @SuppressWarnings({"Duplicates", "BooleanMethodIsAlwaysInverted"})
    private boolean evaluateCondition(ProceedingJoinPoint joinPoint, String condition) {
        if (!StringUtils.isNotBlank(condition)) {
            return true;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = discoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new StandardEvaluationContext();

            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Expression exp = parser.parseExpression(condition);
            Boolean result = exp.getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            // 条件表达式解析失败，默认返回 true
            return true;
        }
    }

}