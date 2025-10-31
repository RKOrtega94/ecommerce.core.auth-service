package ec.com.ecommerce.modules.auth.application.services.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import ec.com.ecommerce.grpc_lib.auth.AuthenticateGrpcResponse;
import ec.com.ecommerce.modules.auth.adapter.persistence.UserSessionRepository;
import ec.com.ecommerce.modules.auth.application.dtos.response.TokenData;
import ec.com.ecommerce.modules.auth.domain.entities.UserSession;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final RSAKey rsaKey;
    private final UserSessionRepository repository;

    @Value("${jwt.access-token.expiration:900}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-token.expiration:86400}")
    private Long refreshTokenExpiration;
    @Value("${jwt.max-sessions:3}")
    private Integer maxSessions;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8080}")
    private String issuer;

    @Override
    @Transactional
    public TokenData generateTokens(AuthenticateGrpcResponse authResponse) {
        String userId = authResponse.getSubject();

        enforceSessionLimit(userId);

        String jti = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(accessTokenExpiration);
        Instant refreshExpiry = now.plusSeconds(refreshTokenExpiration);

        String accessToken;
        String refreshToken;

        try {
            Set<String> authorities = new HashSet<>(authResponse.getRolesList());
            authorities.addAll(new HashSet<>(authResponse.getPermissionsList()));
            JWTPayloadRecord accessPayload = JWTPayloadRecord.builder() //
                    .jti(jti).sub(userId) //
                    .iat(now) //
                    .exp(accessExpiry) //
                    .authorities(authorities) //
                    .type(TokenType.ACCESS.name()) //
                    .additionalClaims(new Object[]{"sessionId", sessionId}) //
                    .build();
            JWTPayloadRecord refreshPayload = JWTPayloadRecord.builder() //
                    .iat(now) //
                    .exp(refreshExpiry) //
                    .type(TokenType.REFRESH.name()) //
                    .additionalClaims(new Object[]{"sessionId", sessionId}) //
                    .build();
            accessToken = generateToken(accessPayload);
            refreshToken = generateToken(refreshPayload);

            // Save session to repository
            UserSession session = UserSession.builder().sessionId(sessionId).userId(userId).roles(new HashSet<>(authResponse.getRolesList())).permissions(new HashSet<>(authResponse.getPermissionsList())).refreshToken(refreshToken).createdAt(now).expiresAt(accessExpiry).refreshExpiresAt(refreshExpiry).revoked(false).build();
            repository.save(session);

            log.debug("Created new session: {} for user: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("Error generating tokens for user: {}", userId, e);
            throw new RuntimeException("Failed to generate tokens", e);
        }

        return new TokenData(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenData refreshToken(String refreshToken) {
        // Find the session by refresh token
        Optional<UserSession> sessionOpt = repository.findByRefreshTokenAndRevokedFalse(refreshToken);
        if (sessionOpt.isEmpty()) {
            log.warn("Invalid or revoked refresh token attempted");
            throw new RuntimeException("Invalid or revoked refresh token");
        }

        UserSession session = sessionOpt.get();

        // Check if refresh token is expired
        if (session.getRefreshExpiresAt().isBefore(Instant.now())) {
            session.setRevoked(true);
            session.setRevokedAt(Instant.now());
            repository.save(session);
            log.warn("Expired refresh token for user: {}", session.getUserId());
            throw new RuntimeException("Refresh token expired");
        }

        // Generate new tokens
        String newJti = UUID.randomUUID().toString();
        String sessionId = session.getSessionId();
        String userId = session.getUserId();

        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(accessTokenExpiration);
        Instant refreshExpiry = now.plusSeconds(refreshTokenExpiration);

        String newAccessToken;
        String newRefreshToken;

        try {
            Set<String> authorities = new HashSet<>();
            if (session.getRoles() != null) {
                authorities.addAll(session.getRoles());
            }
            if (session.getPermissions() != null) {
                authorities.addAll(session.getPermissions());
            }

            JWTPayloadRecord accessPayload = JWTPayloadRecord.builder().jti(newJti).sub(userId).iat(now).exp(accessExpiry).authorities(authorities).type(TokenType.ACCESS.name()).additionalClaims(new Object[]{"sessionId", sessionId}).build();

            JWTPayloadRecord refreshPayload = JWTPayloadRecord.builder().iat(now).exp(refreshExpiry).type(TokenType.REFRESH.name()).additionalClaims(new Object[]{"sessionId", sessionId}).build();

            newAccessToken = generateToken(accessPayload);
            newRefreshToken = generateToken(refreshPayload);

            // Update session with new refresh token and expiration
            session.setRefreshToken(newRefreshToken);
            session.setExpiresAt(accessExpiry);
            session.setRefreshExpiresAt(refreshExpiry);
            repository.save(session);

            log.debug("Refreshed tokens for session: {} user: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("Error refreshing tokens for user: {}", userId, e);
            throw new RuntimeException("Failed to refresh tokens", e);
        }

        return new TokenData(newAccessToken, newRefreshToken);
    }

    /**
     * Generates a JWT token based on the provided payload.
     *
     * @param payload the payload containing JWT claims
     * @return the generated JWT token as a string
     * @throws JOSEException if there is an error during token generation
     */
    private String generateToken(JWTPayloadRecord payload) throws JOSEException {
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder() //
                .issuer(issuer) //
                .issueTime(Date.from(payload.iat())) //
                .expirationTime(Date.from(payload.exp()));
        if (payload.jti() != null) claimsBuilder.jwtID(payload.jti());
        if (payload.sub() != null) claimsBuilder.subject(payload.sub());
        if (payload.authorities() != null && !payload.authorities().isEmpty())
            claimsBuilder.claim("authorities", payload.authorities());
        if (payload.additionalClaims() != null && payload.additionalClaims().length % 2 == 0) {
            for (int i = 0; i < payload.additionalClaims().length; i += 2) {
                String key = (String) payload.additionalClaims()[i];
                Object value = payload.additionalClaims()[i + 1];
                claimsBuilder.claim(key, value);
            }
        }
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).type(JOSEObjectType.JWT).build();
        SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());
        RSASSASigner signer = new RSASSASigner(rsaKey.toRSAPrivateKey());
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    /**
     * Enforce session limit by removing oldest sessions if the limit is exceeded.
     *
     * @param userId the ID of the user
     */
    private void enforceSessionLimit(String userId) {
        List<UserSession> sessions = repository.findByUserIdAndRevokedFalse(userId);

        if (sessions.size() >= maxSessions) {
            sessions.sort(Comparator.comparing(UserSession::getCreatedAt));
            int sessionsToRemove = sessions.size() - maxSessions + 1;
            for (int i = 0; i < sessionsToRemove; i++) {
                UserSession oldestSession = sessions.get(i);
                oldestSession.setRevoked(true);
                oldestSession.setRevokedAt(Instant.now());
                repository.save(oldestSession);
                log.info("Revoked oldest session: {} for user {}", oldestSession.getSessionId(), userId);
            }
        }
    }

    /**
     * Token types for distinguishing between access and refresh tokens.
     */
    private enum TokenType {
        ACCESS, REFRESH
    }

    @With
    @Builder
    private record JWTPayloadRecord(String jti, String sub, Instant iat, Instant exp, Set<String> authorities,
                                    String type, Object[] additionalClaims) {
    }
}