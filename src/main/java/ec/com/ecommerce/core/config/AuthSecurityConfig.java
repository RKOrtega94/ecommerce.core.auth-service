package ec.com.ecommerce.core.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080}")
    private String issuerUri;

    /**
     * Authorization Server Security Filter Chain
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.with(new OAuth2AuthorizationServerConfigurer(), authServer -> authServer.oidc(oidc -> oidc //
                .clientRegistrationEndpoint(Customizer.withDefaults()) //
                .userInfoEndpoint(Customizer.withDefaults())));
        http
                // Stateless session management - no sessions will be created or used by Spring Security
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Exception handling - Return JSON errors instead of redirecting to the login page
                .exceptionHandling(exceptions -> exceptions
                        // Return 401 Unauthorized for unauthenticated requests
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
                        // Access denied handler
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(FORBIDDEN.value());
                            response.setContentType(APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"access_denied\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                        }))
                // Enable OAuth2 Resource Server support with JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        // JWT authentication
                        .jwt(Customizer.withDefaults())
                        // Return 401 Unauthorized for unauthenticated requests
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED)));
        return http.build();
    }

    /**
     * Default Security Filter Chain
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorize requests
                .authorizeHttpRequests(authorize -> authorize //
                        .requestMatchers("/api/auth/**", //
                                "/oauth2/token", //
                                "/.well-known/**", //
                                "/health/**", //
                                "/v3/api-docs/**", //
                                "/swagger-ui/**" //
                        ).permitAll() //
                        .anyRequest().authenticated())
                // HTTP Basic Authentication
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED)))
                // Disable form login
                .formLogin(AbstractHttpConfigurer::disable)
                // Stateless session management - no sessions will be created or used by Spring Security
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Disable CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // Enable CORS with default settings
                .cors(Customizer.withDefaults()) //
                // Exception handling - return JSON errors
                .exceptionHandling(exceptions -> exceptions
                        // Return 401 Unauthorized for unauthenticated requests
                        .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
                        // Access denied handler
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(FORBIDDEN.value());
                            response.setContentType(APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"forbidden\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                        }))
                // Configure headers
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    /**
     * Registered Client Repository
     *
     * @param jdbcTemplate JdbcTemplate
     * @return RegisteredClientRepository
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * OAuth2 Authorization Service
     *
     * @param jdbcTemplate               JdbcTemplate
     * @param registeredClientRepository RegisteredClientRepository
     * @return OAuth2AuthorizationService
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * Password Encoder
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JSON Web Key (JWK) Source
     *
     * @return JWKSource<SecurityContext>
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * RSA Key for JWT signing and verification
     *
     * @return RSAKey
     */
    @Bean
    public RSAKey rsaKey() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(publicKey) //
                .privateKey(privateKey) //
                .keyID(UUID.randomUUID().toString()) //
                .build();
    }

    /**
     * Generate RSA Key Pair
     *
     * @return KeyPair
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
        return keyPair;
    }

    /**
     * JWT Decoder
     *
     * @param jwkSource JWKSource<SecurityContext>
     * @return JwtDecoder
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * JWT Encoder
     *
     * @param jwkSource JWKSource<SecurityContext>
     * @return JwtEncoder
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Authorization Server Settings
     *
     * @return AuthorizationServerSettings
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                // Set the issuer URI
                .issuer(issuerUri)
                // OAuth2 Authorization endpoint: used by clients to obtain authorization grants
                .authorizationEndpoint("/oauth2/authorize")
                // OAuth2 Token endpoint: used by clients to exchange authorization grants for tokens
                .tokenEndpoint("/oauth2/token")
                // OAuth2 Token Introspection endpoint: used to check the validity and meta-information of tokens
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                // OAuth2 Token Revocation endpoint: used to revoke access or refresh tokens
                .tokenRevocationEndpoint("/oauth2/revoke")
                // JWK Set endpoint: exposes the public keys used to verify JWT signatures
                .jwkSetEndpoint("/oauth2/jwks")
                // OIDC UserInfo endpoint: returns claims about the authenticated end-user
                .oidcUserInfoEndpoint("/userinfo")
                // OIDC Dynamic Client Registration endpoint: allows clients to register with the authorization server
                .oidcClientRegistrationEndpoint("/connect/register").build();
    }
}
