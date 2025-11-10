package ec.com.ecommerce.auth.application.dtos.response;

/**
 * Data transfer object representing token information.
 *
 * @param accessToken  the access token
 * @param refreshToken the refresh token
 */
public record TokenData(String accessToken, String refreshToken) {
}
