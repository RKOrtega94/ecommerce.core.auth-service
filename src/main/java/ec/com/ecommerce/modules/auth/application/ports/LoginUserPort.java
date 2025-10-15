package ec.com.ecommerce.modules.auth.application.ports;

import ec.com.ecommerce.grpc_lib.auth.AuthGrpcClient;
import ec.com.ecommerce.grpc_lib.auth.AuthenticateGrpcResponse;
import ec.com.ecommerce.modules.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.modules.auth.application.dtos.response.AuthResponse;
import ec.com.ecommerce.modules.auth.application.dtos.response.TokenData;
import ec.com.ecommerce.modules.auth.application.mappers.AuthMapper;
import ec.com.ecommerce.modules.auth.application.services.jwt.JwtService;
import ec.com.ecommerce.modules.auth.domain.usecases.LoginUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserPort implements LoginUserUseCase {
    private final AuthGrpcClient client;
    private final AuthMapper mapper;
    private final JwtService jwtService;

    @Override
    public AuthResponse execute(LoginRequest request) {
        AuthenticateGrpcResponse response = client.authenticate(mapper.toGrpcRequest(request));
        log.debug("Response from auth service: {}", response.toString());

        TokenData tokenData = jwtService.genefrateTokens(response);
        return new AuthResponse(tokenData.accessToken(), tokenData.refreshToken());
    }
}
