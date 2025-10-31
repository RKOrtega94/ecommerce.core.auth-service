package ec.com.ecommerce.modules.auth.adapter.rest;

import ec.com.ecommerce.models.ApiResponse;
import ec.com.ecommerce.models.SuccessEmptyResponse;
import ec.com.ecommerce.models.SuccessResponse;
import ec.com.ecommerce.modules.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.modules.auth.application.dtos.request.RefreshTokenRequest;
import ec.com.ecommerce.modules.auth.application.dtos.request.RegisterRequest;
import ec.com.ecommerce.modules.auth.application.dtos.response.AuthResponse;
import ec.com.ecommerce.modules.auth.application.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController handles authentication-related endpoints.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication, registration, and token management")
public class AuthController {
    private final AuthService service;

    @Operation(summary = "User login", description = "Authenticate user and return access and refresh tokens.")
    @PostMapping(value = "/login", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> loginForm(@ModelAttribute @Valid LoginRequest request) {
        SuccessResponse<AuthResponse> response = SuccessResponse.ok(service.login(request), "Login successful");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User login", description = "Authenticate user and return access and refresh tokens.")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> loginJson(@RequestBody @Valid LoginRequest request) {
        SuccessResponse<AuthResponse> response = SuccessResponse.ok(service.login(request), "Login successful");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User registration", description = "Register a new user.")
    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> registerForm(@ModelAttribute @Valid RegisterRequest request) {
        service.register(request);
        SuccessEmptyResponse response = SuccessEmptyResponse.created("User registered successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User registration", description = "Register a new user.")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> registerJson(@RequestBody @Valid RegisterRequest request) {
        service.register(request);
        SuccessEmptyResponse response = SuccessEmptyResponse.created("User registered successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh token", description = "Refresh access token using a valid refresh token.")
    @PostMapping(value = "/refresh", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> refreshTokenForm(@ModelAttribute @Valid RefreshTokenRequest request) {
        AuthResponse response = service.refreshToken(request.refreshToken());
        SuccessResponse<AuthResponse> apiResponse = SuccessResponse.ok(response, "Token refreshed successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Refresh token", description = "Refresh access token using a valid refresh token.")
    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> refreshTokenJson(@RequestBody @Valid RefreshTokenRequest request) {
        AuthResponse response = service.refreshToken(request.refreshToken());
        SuccessResponse<AuthResponse> apiResponse = SuccessResponse.ok(response, "Token refreshed successfully");
        return ResponseEntity.ok(apiResponse);
    }
}
