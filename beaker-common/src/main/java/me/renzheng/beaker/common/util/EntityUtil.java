package me.renzheng.beaker.common.util;

import me.renzheng.beaker.common.context.IdentityContextHolder;
import me.renzheng.beaker.common.entity.AbstractEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EntityUtil
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
public class EntityUtil {

    public static <T extends AbstractEntity<?>> void populateCreationFields(T entity) {
        entity.setCreator(IdentityContextHolder.getIdentity());
        entity.setModifier(IdentityContextHolder.getIdentity());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDeleted(false);
    }

    public static <T extends AbstractEntity<?>> void populateCreationFields(List<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        entities.forEach(EntityUtil::populateCreationFields);
    }

    public static <T extends AbstractEntity<?>> void populateUpdateFields(T entity) {
        entity.setModifier(IdentityContextHolder.getIdentity());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public static <T extends AbstractEntity<?>> void populateUpdateFields(List<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        entities.forEach(EntityUtil::populateUpdateFields);
    }
}
