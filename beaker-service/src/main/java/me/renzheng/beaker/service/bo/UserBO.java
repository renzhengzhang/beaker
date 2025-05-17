package me.renzheng.beaker.service.bo;

import lombok.Getter;
import lombok.Setter;
import me.renzheng.beaker.common.entity.AbstractEntity;
import me.renzheng.beaker.common.enums.Gender;
import me.renzheng.beaker.common.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * UserBO
 *
 * @author Renzheng Zhang
 * @since 2024/4/28
 */
@Getter
@Setter
public class UserBO extends AbstractEntity<Long> implements UserDetails {

    private String username;

    private Gender gender;

    private LocalDate birthday;

    private String phoneNumber;

    private String email;

    private String passwd;

    private Boolean banned;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(Role.USER.name());
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(banned);
    }
}
