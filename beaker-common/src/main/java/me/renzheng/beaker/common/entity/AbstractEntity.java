package me.renzheng.beaker.common.entity;

import java.time.LocalDateTime;

/**
 * Abstract Entity
 *
 * @author Renzheng Zhang
 * @since 2024/4/18
 */
public abstract class AbstractEntity<T> {
    protected T id;

    protected String creator;

    protected String modifier;

    protected LocalDateTime createdAt;

    protected LocalDateTime updatedAt;

    protected boolean deleted;
}
