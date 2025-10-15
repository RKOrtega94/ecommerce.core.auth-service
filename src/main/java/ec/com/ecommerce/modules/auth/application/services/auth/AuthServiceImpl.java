package ec.com.ecommerce.modules.auth.application.services.auth;

import ec.com.ecommerce.modules.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.modules.auth.application.dtos.request.RegisterRequest;
import ec.com.ecommerce.modules.auth.application.dtos.response.AuthResponse;
import ec.com.ecommerce.modules.auth.application.services.jwt.JwtService;
import ec.com.ecommerce.modules.auth.domain.usecases.LoginUserUseCase;
import ec.com.ecommerce.modules.auth.domain.usecases.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final LoginUserUseCase loginUser;
    private final RegisterUseCase register;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        return loginUser.execute(request);
    }

    @Override
    public void register(RegisterRequest request) {
        register.execute(request);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        var tokenData = jwtService.refreshToken(refreshToken);
        return new AuthResponse(tokenData.accessToken(), tokenData.refreshToken());
    }
}
