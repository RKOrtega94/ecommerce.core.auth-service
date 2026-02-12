package ec.com.ecommerce.auth.application.services.auth;

import ec.com.ecommerce.auth.application.dtos.request.GuestTokenRequest;
import ec.com.ecommerce.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.auth.application.dtos.request.RegisterRequest;
import ec.com.ecommerce.auth.application.dtos.response.AuthResponse;
import jakarta.validation.Valid;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {
    /**
     * Handles user login.
     *
     * @param request the login request containing user credentials
     * @return the authentication response containing tokens and user info
     */
    AuthResponse login(LoginRequest request);

    void register(RegisterRequest request);

    /**
     * Refreshes authentication tokens using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return the authentication response containing new tokens
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Generates a guest token for unauthenticated access.
     *
     * @param request the guest token request containing subject and claims
     * @return the authentication response containing guest token and user info
     */
    AuthResponse guestToken(@Valid GuestTokenRequest request);
}
