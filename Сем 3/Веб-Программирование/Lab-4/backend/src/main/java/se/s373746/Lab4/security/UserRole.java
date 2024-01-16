package se.s373746.Lab4.security;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_DEVELOPER;

    @Override
    public String getAuthority() {
        return name();
    }
}
