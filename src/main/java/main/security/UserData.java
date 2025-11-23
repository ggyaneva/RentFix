package main.security;

import lombok.AllArgsConstructor;
import main.model.User;
import main.model.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserData implements UserDetails {

    private UUID userId;
    private String username;
    private String password;
    private Role role;
    private boolean accountActive;

    public static UserData from(User user) {
        return new UserData(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                true
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public boolean isAccountNonExpired() { return accountActive; }
    @Override public boolean isAccountNonLocked() { return accountActive; }
    @Override public boolean isCredentialsNonExpired() { return accountActive; }
    @Override public boolean isEnabled() { return accountActive; }
}
