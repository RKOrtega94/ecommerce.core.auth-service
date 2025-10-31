package ec.com.ecommerce.modules.auth.application.services.jwt;

import ec.com.ecommerce.grpc_lib.auth.AuthenticateGrpcResponse;
import ec.com.ecommerce.modules.auth.application.dtos.response.TokenData;

/**
 * Service for handling JWT operations such as token generation.
 */
public interface JwtService {
    /**
     * Generates a JWT token for the given user details.
     *
     * @param authResponse the details of the user for whom the token is to be generated
     * @return the generated JWT token
     */
    TokenData generateTokens(AuthenticateGrpcResponse authResponse);

    /**
     * Refreshes authentication tokens using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return new access and refresh tokens
     */
    TokenData refreshToken(String refreshToken);
}
