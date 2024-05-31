package me.renzheng.beaker.dao.bo;

import lombok.Getter;
import lombok.Setter;
import me.renzheng.beaker.common.entity.AbstractEntity;
import me.renzheng.beaker.common.enums.Gender;

import java.time.LocalDate;

/**
 * UserBO
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Getter
@Setter
public class UserBO extends AbstractEntity<Long> {

    private String username;

    private Gender gender;

    private LocalDate birthday;

    private String phoneNumber;

    private String email;

    private String passwd;

    private Boolean banned;
}
