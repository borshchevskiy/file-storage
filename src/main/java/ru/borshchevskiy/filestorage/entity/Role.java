package ru.borshchevskiy.filestorage.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * Represents User's role. Mostly required as implementation of Spring Security's {@link GrantedAuthority}
 *
 * @see GrantedAuthority
 */
public enum Role implements GrantedAuthority {
    ROLE_USER,
    ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
