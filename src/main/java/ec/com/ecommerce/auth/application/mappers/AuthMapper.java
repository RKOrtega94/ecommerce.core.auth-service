package ec.com.ecommerce.auth.application.mappers;

import ec.com.ecommerce.grpc_lib.auth.AuthenticateRequest;
import ec.com.ecommerce.grpc_lib.auth.RegisterGrpcRequest;
import ec.com.ecommerce.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.auth.application.dtos.request.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Mapper for authentication requests.
 */
@Mapper(componentModel = SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {
    /**
     * Convert LoginRequest to gRPC AuthenticateRequest.
     *
     * @param request the login request
     * @return the gRPC authenticate request
     */
    AuthenticateRequest toGrpcRequest(LoginRequest request);

    /**
     * Convert RegisterRequest to gRPC RegisterGrpcRequest.
     *
     * @param request the register request
     * @return the gRPC register request
     */
    RegisterGrpcRequest toGrpcRequest(RegisterRequest request);
}