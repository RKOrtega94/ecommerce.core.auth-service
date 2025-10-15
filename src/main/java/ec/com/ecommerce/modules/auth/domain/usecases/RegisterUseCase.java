package ec.com.ecommerce.modules.auth.domain.usecases;

import ec.com.ecommerce.modules.auth.application.dtos.request.RegisterRequest;

/**
 * Use case for registering a new user.
 */
public interface RegisterUseCase {
    /**
     * Executes the registration process for a new user.
     *
     * @param request the registration request containing user details
     */
    void execute(RegisterRequest request);
}
