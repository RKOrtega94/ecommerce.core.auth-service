package ec.com.ecommerce.modules.auth.domain.usecases;

import ec.com.ecommerce.modules.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.modules.auth.application.dtos.response.AuthResponse;

/**
 * Use case for logging in a user.
 */
public interface LoginUserUseCase {
    /**
     * Executes the login process for a user.
     *
     * @param request the login request containing user credentials
     * @return an object representing the result of the login operation
     */
    AuthResponse execute(LoginRequest request);
}
