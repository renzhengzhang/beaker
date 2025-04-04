package me.renzheng.beaker.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

public class IdentityContextHolder {

    private static final ThreadLocal<IdentityContext> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

    public static IdentityContext getIdentityContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void setIdentityContext(IdentityContext identityContext) {
        CONTEXT_HOLDER.set(identityContext);
    }

    public static void removeIdentityContext() {
        CONTEXT_HOLDER.remove();
    }

    public static Long getIdentity() {
        IdentityContext identityContext = getIdentityContext();
        return Optional.ofNullable(identityContext).map(IdentityContext::getId).orElse(0L);
    }
}
