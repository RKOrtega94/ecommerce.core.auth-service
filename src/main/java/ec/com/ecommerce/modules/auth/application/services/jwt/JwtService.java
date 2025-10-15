package ec.com.ecommerce.modules.auth.application.services;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service for handling JWT operations such as token generation.
 */
public interface JwtService {
    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails the details of the user for whom the token is to be generated
     * @return the generated JWT token
     */
    String genefrateToken(UserDetails userDetails);

    /**
     * Generates a refresh token for the given user details.
     *
     * @param userDetails the details of the user for whom the refresh token is to be generated
     * @return the generated refresh token
     */
    String generateRefreshToken(UserDetails userDetails);
}
