package ec.com.ecommerce.auth.application.ports.in;

import ec.com.ecommerce.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.auth.application.dtos.response.AuthResponse;
import ec.com.ecommerce.auth.application.services.jwt.JwtService;
import ec.com.ecommerce.auth.domain.usecases.LoginUserUseCase;
import ec.com.ecommerce.exceptions.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserPort implements LoginUserUseCase {
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse execute(LoginRequest request) {
        var userDetails = userDetailsService.loadUserByUsername(request.username());
        if (!passwordsMatch(request.password(), userDetails.getPassword())) throw new InvalidCredentialsException();
        var tokenData = jwtService.generateTokens(userDetails);
        return AuthResponse.builder() //
                .accessToken(tokenData.accessToken()) //
                .refreshToken(tokenData.refreshToken()) //
                .build();
    }

    private boolean passwordsMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
