package ec.com.ecommerce.auth.application.dtos.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing authentication tokens.
 *
 * @param refreshToken the refresh token to use for generating new tokens
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}

