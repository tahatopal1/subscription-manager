package com.project.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Custom principal that carries the database Long ID natively.
 *
 * Using this instead of the generic {@link org.springframework.security.core.userdetails.User}
 * eliminates all string-parsing of the security context — controllers inject this directly
 * via {@code @AuthenticationPrincipal CustomUserDetails} and access {@link #getId()} with
 * full type safety.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long   id;
    private final String username;   // email
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonLocked;

    public CustomUserDetails(Long id, String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean accountNonLocked) {
        this.id               = id;
        this.username         = username;
        this.password         = password;
        this.authorities      = authorities;
        this.accountNonLocked = accountNonLocked;
    }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}
