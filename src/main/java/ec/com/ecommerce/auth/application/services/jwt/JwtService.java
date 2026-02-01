package ec.com.ecommerce.auth.application.services.jwt;

import ec.com.ecommerce.auth.application.dtos.response.TokenData;
import ec.com.ecommerce.remote.security.entities.UserEntity;

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
    TokenData generateTokens(UserEntity entity);

    /**
     * Refreshes authentication tokens using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return new access and refresh tokens
     */
    TokenData refreshToken(String refreshToken);
}
