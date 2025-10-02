package ec.com.ecommerce.modules.auth.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * LoginRequest represents the data required for a login operation.
 *
 * @param username the username of the user attempting to log in
 * @param password the password of the user attempting to log in
 */
public record LoginRequest(@NotNull @NotEmpty @NotBlank String username, @NotNull @NotEmpty @NotBlank String password) {
}
