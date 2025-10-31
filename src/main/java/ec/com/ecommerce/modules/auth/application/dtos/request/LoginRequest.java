package ec.com.ecommerce.modules.auth.application.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * LoginRequest represents the data required for a login operation.
 *
 * @param username the username of the user attempting to log in
 * @param password the password of the user attempting to log in
 */
@Schema(description = "LoginRequest", example = """
        {
          "username": "johndoe",
          "password": "securePassword123",
          "rememberMe": true
        }
        """)
public record LoginRequest(
        @Schema(description = "The username of the user", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty @NotBlank String username,
        @Schema(description = "The password of the user", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty @NotBlank String password,
        @Schema(description = "Flag to indicate if the user wants to be remembered", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED) Boolean rememberMe) {
}
