package ec.com.ecommerce.remote.security.projections;

import java.time.Instant;
import java.util.Set;

/**
 * DTO implementation of LoginUserProjection for user login details.
 */
public record LoginUserView(
        String username,
        String password,
        Instant passwordExpiration,
        Set<String> roles,
        Set<String> permissions,
        Set<String> directPermissions
) implements LoginUserProjection {

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Instant getPasswordExpiration() {
        return passwordExpiration;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Set<String> getDirectPermissions() {
        return directPermissions;
    }
}
