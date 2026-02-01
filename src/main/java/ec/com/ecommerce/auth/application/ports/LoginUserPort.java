package ec.com.ecommerce.auth.application.ports;

import ec.com.ecommerce.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.auth.application.dtos.response.AuthResponse;
import ec.com.ecommerce.auth.application.services.jwt.JwtService;
import ec.com.ecommerce.auth.domain.usecases.LoginUserUseCase;
import ec.com.ecommerce.remote.security.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserPort implements LoginUserUseCase {
    private final UserRepository userRepository;

    private final JwtService jwtService;

    @Override
    public AuthResponse execute(LoginRequest request) {
        var user = userRepository.findByUsername(request.username()).orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        var tokenData = jwtService.generateTokens(user);
        return new AuthResponse(tokenData.accessToken(), tokenData.refreshToken());
    }
}
