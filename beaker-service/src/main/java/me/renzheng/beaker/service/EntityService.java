package me.renzheng.beaker.service;

/**
 * AbstractEntityService
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
public interface EntityService<T, E> {

    void insert(E entity);

    void update(E entity);

    E select(T id);

    void deleteByPrimaryKey(T id);
}
