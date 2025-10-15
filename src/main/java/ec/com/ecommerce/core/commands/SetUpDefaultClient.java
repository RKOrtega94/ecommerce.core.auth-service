package ec.com.ecommerce.core.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetUpDefaultClient {
    private final RegisteredClientRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${oauth2.client.secret:secret}")
    private String clientSecret;
    @Value("${oauth2.client.redirect-uris:http://localhost:8080/login/oauth2/code/ecommerce-client-oidc,http://localhost:8080/authorized}")
    private List<String> redirectUris;

    private static final String CLIENT_ID = "ecommerce-client";

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application is ready. Setting up default OAuth2 client...");
        RegisteredClient client = repository.findByClientId(CLIENT_ID);
        if (client == null) {
            RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString()) //
                    .clientId(CLIENT_ID) //
                    .clientSecret(passwordEncoder.encode(clientSecret)) //
                    .scope(OidcScopes.OPENID) //
                    .scope(OidcScopes.PROFILE) //
                    .scope("read") //
                    .scope("write") //
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) //
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) //
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS) //
                    .redirectUris(uri -> uri.addAll(redirectUris)) //
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT) //
                    .tokenSettings(TokenSettings.builder() //
                            .accessTokenTimeToLive(Duration.ofMinutes(15)) //
                            .refreshTokenTimeToLive(Duration.ofDays(7)) //
                            .reuseRefreshTokens(false) //
                            .build()) //
                    .clientSettings(ClientSettings.builder() //
                            .requireAuthorizationConsent(true) //
                            .requireProofKey(true) //
                            .tokenEndpointAuthenticationSigningAlgorithm(SignatureAlgorithm.RS256) //
                            .build()) //
                    .build();
            repository.save(registeredClient);
            log.info("Default OAuth2 client '{}' has been created successfully.", CLIENT_ID);
        } else {
            log.info("Default OAuth2 client '{}' already exists. Skipping creation.", CLIENT_ID);
        }
    }
}
