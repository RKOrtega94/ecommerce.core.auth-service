package ec.com.ecommerce.auth.domain.usecases;

import ec.com.ecommerce.remote.security.projections.LoginUserProjection;

public interface FindUserByUsernameUseCase {
    LoginUserProjection execute(String username);
}
