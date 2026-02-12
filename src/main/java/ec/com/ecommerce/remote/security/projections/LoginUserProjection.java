package ec.com.ecommerce.remote.security.projections;

import java.time.Instant;
import java.util.Set;

/**
 * Projection interface for retrieving user login details.
 */
public interface LoginUserProjection {
    String getUsername();

    String getPassword();

    Instant getPasswordExpiration();

    Set<String> getRoles();

    Set<String> getPermissions();

    Set<String> getDirectPermissions();
}
