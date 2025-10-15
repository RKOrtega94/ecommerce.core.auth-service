package ec.com.ecommerce.modules.auth.application.ports;

import ec.com.ecommerce.grpc_lib.auth.AuthGrpcClient;
import ec.com.ecommerce.modules.auth.application.dtos.request.RegisterRequest;
import ec.com.ecommerce.modules.auth.application.mappers.AuthMapper;
import ec.com.ecommerce.modules.auth.domain.usecases.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterPort implements RegisterUseCase {
    private final AuthMapper mapper;
    private final AuthGrpcClient client;

    @Override
    public void execute(RegisterRequest request) {
        client.register(mapper.toGrpcRequest(request));
    }
}
