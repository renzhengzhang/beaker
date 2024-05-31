package me.renzheng.beaker.dao.entity;

import lombok.Getter;
import lombok.Setter;
import me.renzheng.beaker.common.entity.AbstractEntity;

import java.time.LocalDate;

@Getter
@Setter
public class UserDO extends AbstractEntity<Long> {

    private String username;

    private Byte gender;

    private LocalDate birthday;

    private String phoneNumber;

    private String email;

    private String passwd;

    private Boolean banned;
}