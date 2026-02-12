package ec.com.ecommerce.auth.application.services.user;

import ec.com.ecommerce.auth.domain.usecases.FindUserByUsernameUseCase;
import ec.com.ecommerce.exceptions.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {
    private final FindUserByUsernameUseCase findUserByUsername;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userEntity = findUserByUsername.execute(username);
        if (userEntity.getPasswordExpiration() != null && userEntity.getPasswordExpiration().isBefore(Instant.now()))
            throw new InvalidCredentialsException();
        var roles = userEntity.getRoles();
        var permissions = userEntity.getPermissions();
        var directPermissions = userEntity.getDirectPermissions();
        permissions.addAll(directPermissions);
        return User.withUsername(userEntity.getUsername()) //
                .password(userEntity.getPassword()) //
                .authorities(getAuthorities(roles, permissions)) //
                .accountExpired(false) //
                .accountLocked(false) //
                .credentialsExpired(credentialsExpired(userEntity.getPasswordExpiration())) //
                .disabled(false) //
                .build();
    }

    private SimpleGrantedAuthority mapToAuthority(String permission) {
        return new SimpleGrantedAuthority(permission);
    }

    private GrantedAuthority mapToRole(String role) {
        return new SimpleGrantedAuthority("ROLE_" + role);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<String> roles, Collection<String> permissions) {
        var authorities = new java.util.ArrayList<>(roles.stream().map(this::mapToRole).toList());
        var perms = permissions.stream().map(this::mapToAuthority).toList();
        authorities.addAll(perms);
        return authorities;
    }

    private Boolean credentialsExpired(Instant expiration) {
        return expiration != null && expiration.isBefore(Instant.now());
    }
}
