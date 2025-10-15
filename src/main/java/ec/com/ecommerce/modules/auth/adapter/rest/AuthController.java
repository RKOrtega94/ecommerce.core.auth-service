package ec.com.ecommerce.modules.auth.adapter.rest;

import ec.com.ecommerce.modules.auth.application.dtos.request.LoginRequest;
import ec.com.ecommerce.modules.auth.application.dtos.request.RefreshTokenRequest;
import ec.com.ecommerce.modules.auth.application.dtos.request.RegisterRequest;
import ec.com.ecommerce.modules.auth.application.dtos.response.AuthResponse;
import ec.com.ecommerce.modules.auth.application.services.auth.AuthService;
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
public class AuthController {
    private final AuthService service;

    @PostMapping(value = "/login", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> loginForm(@ModelAttribute @Valid LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> loginJson(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerForm(@ModelAttribute @Valid RegisterRequest request) {
        service.register(request);
        return "User registered successfully";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerJson(@RequestBody @Valid RegisterRequest request) {
        service.register(request);
        return "User registered successfully";
    }

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        AuthResponse response = service.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}
