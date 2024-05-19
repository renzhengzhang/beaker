package me.renzheng.beaker.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Abstract Entity
 *
 * @author Renzheng Zhang
 * @since 2024/4/18
 */
@Getter
@Setter
public abstract class AbstractEntity<T> {
    protected T id;

    protected Long creator;

    protected Long modifier;

    protected LocalDateTime createdAt;

    protected LocalDateTime updatedAt;

    protected boolean deleted;
}
