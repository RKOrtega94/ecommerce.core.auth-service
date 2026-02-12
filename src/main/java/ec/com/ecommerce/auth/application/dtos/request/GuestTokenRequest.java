package ec.com.ecommerce.auth.application.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "GuestTokenRequest", description = "Guest token request")
public record GuestTokenRequest(String subject, Map<String, String> claims, Long expirationInMillis) {
}
