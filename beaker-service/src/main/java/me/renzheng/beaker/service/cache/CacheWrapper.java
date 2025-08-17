package me.renzheng.beaker.service.cache;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 缓存包装类
 *
 * @author Renzheng Zhang
 */
@Getter
@Setter
public class CacheWrapper implements Serializable {

    @Serial
    private static final long serialVersionUID = -6018348158214027258L;

    // 使用字符串常量代替空对象
    private static final String NULL_PLACEHOLDER = "__NULL__";

    private Object value;

    @JsonCreator
    public CacheWrapper(@JsonProperty("value") Object value) {
        this.value = value;
    }

    public boolean containsValue() {
        return !NULL_PLACEHOLDER.equals(value);
    }

    public static CacheWrapper of(Object value) {
        if (Objects.isNull(value)) {
            return ofEmpty();
        }
        return new CacheWrapper(value);
    }

    public static CacheWrapper ofEmpty() {
        return new CacheWrapper(NULL_PLACEHOLDER);
    }

    public Object getValue() {
        return NULL_PLACEHOLDER.equals(value) ? null : value;
    }

}
