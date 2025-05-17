package me.renzheng.beaker.common.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.renzheng.beaker.common.constants.IdentityConstants;

@Getter
@Setter
@Builder
public class IdentityContext {
    private Long id;
    private String username;

    public static IdentityContext anonymous() {
        return IdentityContext.builder()
                .id(IdentityConstants.ANONYMOUS_ID)
                .username(IdentityConstants.ANONYMOUS_USERNAME)
                .build();
    }

    public static IdentityContext system() {
        return IdentityContext.builder()
                .id(IdentityConstants.SYSTEM_ID)
                .username(IdentityConstants.SYSTEM_USERNAME)
                .build();
    }
}
