package ec.com.ecommerce.auth.application.dtos.response;

import lombok.Builder;
import lombok.With;

/**
 * Authentication response DTO.
 *
 * @param accessToken  token used for accessing protected resources
 * @param refreshToken token used to obtain a new access token
 */
@With
@Builder
public record AuthResponse(String accessToken, String refreshToken) {
}
