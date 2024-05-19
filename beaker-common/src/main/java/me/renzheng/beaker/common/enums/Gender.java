package me.renzheng.beaker.common.enums;

/**
 * 性别枚举
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
public enum Gender {
    UNKNOWN((byte)-1, "未知"),
    FEMALE((byte)0, "女"),
    MALE((byte)1, "男");

    Gender(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final byte code;

    private final String desc;

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static Gender fromCode(byte code) {
        for (Gender gender : Gender.values()) {
            if (gender.code == code) {
                return gender;
            }
        }

        return UNKNOWN;
    }
}
