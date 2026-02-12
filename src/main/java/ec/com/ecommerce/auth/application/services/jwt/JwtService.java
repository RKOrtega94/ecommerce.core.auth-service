package ec.com.ecommerce.auth.application.services.jwt;

import ec.com.ecommerce.auth.application.dtos.response.TokenData;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * Service for handling JWT operations such as token generation.
 */
public interface JwtService {
    /**
     * Generates a JWT token for the given user details.
     *
     * @param entity the details of the user for whom the token is to be generated
     * @return the generated JWT token
     */
    TokenData generateTokens(UserDetails entity);

    /**
     * Generates a guest token for unauthenticated access.
     *
     * @param subject            user identifier
     * @param claims             additional claims for the token
     * @param expirationInMillis token expiration time in milliseconds
     * @return generated guest token data
     */
    TokenData generateGuestToken(String subject, Map<String, Object> claims, Long expirationInMillis);

    /**
     * Refreshes authentication tokens using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return new access and refresh tokens
     */
    TokenData refreshToken(String refreshToken);

    /**
     * Revokes a session, invalidating all associated tokens.
     *
     * @param sessionId the session ID to revoke
     * @return true if the session was successfully revoked, false otherwise
     */
    boolean revokeSession(String sessionId);

    /**
     * Revokes all sessions for a specific user.
     *
     * @param userId the user ID whose sessions should be revoked
     * @return the number of sessions revoked
     */
    int revokeAllUserSessions(String userId);
}
