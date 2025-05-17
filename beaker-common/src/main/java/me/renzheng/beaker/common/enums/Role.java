package me.renzheng.beaker.common.enums;

import lombok.Getter;

/**
 * 角色
 *
 * @author <a href="mailto:renzheng.zh@gmail.com">Renzheng Zhang</a>
 */
@Getter
public enum Role {
    ADMIN,
    USER,
    ;

    public static Role from(String role) {
        for (Role value : values()) {
            if (value.name().equalsIgnoreCase(role)) {
                return value;
            }
        }
        return null;
    }
}
