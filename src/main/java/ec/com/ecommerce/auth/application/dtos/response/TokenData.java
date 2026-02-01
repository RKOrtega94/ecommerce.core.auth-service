package ec.com.ecommerce.auth.application.dtos.response;

import lombok.Builder;

/**
 * Data transfer object representing token information.
 *
 * @param accessToken  the access token
 * @param refreshToken the refresh token
 */
@Builder
public record TokenData(String accessToken, String refreshToken) {
}
