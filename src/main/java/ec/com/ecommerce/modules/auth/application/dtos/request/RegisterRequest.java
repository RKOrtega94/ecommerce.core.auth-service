package ec.com.ecommerce.modules.auth.application.dtos.request;

import ec.com.ecommerce.modules.auth.application.dtos.validators.ConfirmPassword;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * RegisterRequest represents the data required for a user registration operation.
 *
 * @param firstname       the first name of the user
 * @param lastname        the last name of the user
 * @param email           the email address of the user
 * @param password        the password for the user account
 * @param confirmPassword the confirmation of the password for the user account
 */
@ConfirmPassword
public record RegisterRequest(
        @NotNull @NotBlank @NotEmpty @Length(min = 3, max = 64) @Pattern(regexp = "^[a-zA-Z]+$", message = "Must be a valid name") String firstname,
        @NotNull @NotBlank @NotEmpty @Length(min = 3, max = 64) @Pattern(regexp = "^[a-zA-Z]+$", message = "Must be a valid name") String lastname,
        @Email @NotNull @NotBlank @NotEmpty @Length(min = 5, max = 128) @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email address") String username,
        @NotNull @NotBlank @NotEmpty @Length(min = 3, max = 32) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[./,&%$@*()_\\-!])[A-Za-z\\d./,&%$@*()_\\-!]{3,32}$", message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character (./,&%$@*()_-!)") String password,
        @NotNull @NotBlank @NotEmpty String confirmPassword) {
}
