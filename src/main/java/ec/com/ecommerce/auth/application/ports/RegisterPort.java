package ec.com.ecommerce.auth.application.ports;

import ec.com.ecommerce.grpc_lib.auth.AuthGrpcClient;
import ec.com.ecommerce.auth.application.dtos.request.RegisterRequest;
import ec.com.ecommerce.auth.application.mappers.AuthMapper;
import ec.com.ecommerce.auth.domain.usecases.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterPort implements RegisterUseCase {
    private final AuthMapper mapper;
    private final AuthGrpcClient client;

    @Override
    public void execute(RegisterRequest request) {
        var response = client.register(mapper.toGrpcRequest(request));
        if (!response.getSuccess()) throw new IllegalArgumentException("Registration failed: " + response.getMessage());
    }
}
