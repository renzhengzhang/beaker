package me.renzheng.beaker.common.util;

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
        // TODO set creator and modifier with spring security
        entity.setCreator(0L);
        entity.setModifier(0L);
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
        // TODO set modifier
        entity.setModifier(0L);
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public static <T extends AbstractEntity<?>> void populateUpdateFields(List<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        entities.forEach(EntityUtil::populateUpdateFields);
    }
}
