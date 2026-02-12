package ec.com.ecommerce.auth.application.services.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import ec.com.ecommerce.auth.adapter.persistence.UserSessionRepository;
import ec.com.ecommerce.auth.application.dtos.response.TokenData;
import ec.com.ecommerce.auth.domain.entities.UserSession;
import ec.com.ecommerce.auth.domain.exceptions.InvalidRefreshTokenException;
import ec.com.ecommerce.auth.domain.exceptions.TokenGenerationException;
import lombok.Builder;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String GUEST_ROLE = "ROLE_GUEST";

    private final RSAKey rsaKey;
    private final UserSessionRepository repository;
    private final RSASSASigner signer;

    @Value("${jwt.access-token.expiration:900}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-token.expiration:86400}")
    private Long refreshTokenExpiration;
    @Value("${jwt.max-sessions:3}")
    private Integer maxSessions;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8083}")
    private String issuer;

    public JwtServiceImpl(RSAKey rsaKey, UserSessionRepository repository) {
        this.rsaKey = rsaKey;
        this.repository = repository;
        try {
            this.signer = new RSASSASigner(rsaKey.toRSAPrivateKey());
        } catch (JOSEException e) {
            log.error("Failed to initialize RSA signer", e);
            throw new TokenGenerationException("Failed to initialize RSA signer", e);
        }
    }

    @Override
    @Transactional
    public TokenData generateTokens(UserDetails entity) {
        var userId = entity.getUsername();
        enforceSessionLimit(userId);
        var jti = UUID.randomUUID().toString();
        var sessionId = UUID.randomUUID().toString();
        var now = Instant.now();
        var accessExpiry = now.plusSeconds(accessTokenExpiration);
        var refreshExpiry = now.plusSeconds(refreshTokenExpiration);
        Set<String> authorities = new HashSet<>();
        entity.getAuthorities().forEach(authority -> authorities.add(authority.getAuthority()));
        var accessPayload = createPayloadRecord(jti, userId, now, accessExpiry, authorities, TokenType.ACCESS, SESSION_ID_CLAIM, sessionId);
        var refreshPayload = createPayloadRecord(null, null, now, refreshExpiry, null, TokenType.REFRESH, SESSION_ID_CLAIM, sessionId);
        String accessToken;
        String refreshToken;
        try {
            accessToken = generateToken(accessPayload);
            refreshToken = generateToken(refreshPayload);
        } catch (JOSEException e) {
            log.error("Error generating tokens for user: {}", userId, e);
            throw new TokenGenerationException("Failed to generate tokens for user: " + userId, e);
        }
        var userSession = UserSession.builder() //
                .sessionId(sessionId) //
                .userId(userId) //
                .roles(entity.getAuthorities().stream().filter(auth -> auth.getAuthority().startsWith("ROLE_")).map(auth -> auth.getAuthority().substring(5)).collect(Collectors.toSet())) //
                .permissions(authorities) //
                .refreshToken(refreshToken) //
                .createdAt(now) //
                .expiresAt(accessExpiry) //
                .refreshExpiresAt(refreshExpiry) //
                .revoked(false) //
                .build();
        repository.save(userSession);
        log.info("Security audit: New session created - sessionId: {}, userId: {}", sessionId, userId);
        log.debug("Generated tokens for session: {} user: {}", sessionId, userId);
        return new TokenData(accessToken, refreshToken);
    }

    @Override
    public TokenData generateGuestToken(String subject, Map<String, Object> claims, Long expirationInMillis) {
        var userId = subject;
        enforceSessionLimit(userId);
        var jti = UUID.randomUUID().toString();
        var sessionId = UUID.randomUUID().toString();
        var now = Instant.now();
        if (expirationInMillis == null || expirationInMillis <= 0) {
            throw new TokenGenerationException("Guest token expiration must be a positive number of milliseconds");
        }
        var accessExpiry = now.plusMillis(expirationInMillis);
        var refreshExpiry = now.plusSeconds(refreshTokenExpiration);

        Set<String> authorities = new HashSet<>();
        authorities.add(GUEST_ROLE);
        Set<String> guestPermissions = extractGuestPermissions(claims);
        authorities.addAll(guestPermissions);

        var accessPayload = createPayloadRecord(jti, userId, now, accessExpiry, authorities, TokenType.ACCESS, SESSION_ID_CLAIM, sessionId);
        var refreshPayload = createPayloadRecord(null, null, now, refreshExpiry, null, TokenType.REFRESH, SESSION_ID_CLAIM, sessionId);
        String accessToken;
        String refreshToken;
        try {
            accessToken = generateToken(accessPayload);
            refreshToken = generateToken(refreshPayload);
        } catch (JOSEException e) {
            log.error("Error generating guest tokens for user: {}", userId, e);
            throw new TokenGenerationException("Failed to generate guest tokens for user: " + userId, e);
        }

        var userSession = UserSession.builder() //
                .sessionId(sessionId) //
                .userId(userId) //
                .roles(Set.of("GUEST")) //
                .permissions(null) //
                .refreshToken(refreshToken) //
                .createdAt(now) //
                .expiresAt(accessExpiry) //
                .refreshExpiresAt(refreshExpiry) //
                .revoked(false) //
                .build();
        repository.save(userSession);
        log.info("Security audit: New guest session created - sessionId: {}, userId: {}", sessionId, userId);
        log.debug("Generated guest tokens for session: {} user: {}", sessionId, userId);
        return new TokenData(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenData refreshToken(String refreshToken) {
        // Validate input
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Security audit: Attempted refresh with null or empty token");
            throw new InvalidRefreshTokenException("Refresh token cannot be null or empty");
        }

        // Find the session by refresh token
        Optional<UserSession> sessionOpt = repository.findByRefreshTokenAndRevokedFalse(refreshToken);
        if (sessionOpt.isEmpty()) {
            log.warn("Security audit: Invalid or revoked refresh token attempted");
            throw new InvalidRefreshTokenException("Invalid or revoked refresh token");
        }

        UserSession session = sessionOpt.get();

        // Check if refresh token is expired
        if (session.getRefreshExpiresAt().isBefore(Instant.now())) {
            session.setRevoked(true);
            session.setRevokedAt(Instant.now());
            repository.save(session);
            log.warn("Security audit: Expired refresh token for user: {}, sessionId: {}", session.getUserId(), session.getSessionId());
            throw new InvalidRefreshTokenException("Refresh token expired");
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

            JWTPayloadRecord accessPayload = JWTPayloadRecord.builder().jti(newJti).sub(userId).iat(now).exp(accessExpiry).authorities(authorities).type(TokenType.ACCESS.name()).additionalClaims(new Object[]{SESSION_ID_CLAIM, sessionId}).build();

            JWTPayloadRecord refreshPayload = JWTPayloadRecord.builder().iat(now).exp(refreshExpiry).type(TokenType.REFRESH.name()).additionalClaims(new Object[]{SESSION_ID_CLAIM, sessionId}).build();

            newAccessToken = generateToken(accessPayload);
            newRefreshToken = generateToken(refreshPayload);

            // Update session with new refresh token and expiration
            session.setRefreshToken(newRefreshToken);
            session.setExpiresAt(accessExpiry);
            session.setRefreshExpiresAt(refreshExpiry);
            repository.save(session);

            log.info("Security audit: Tokens refreshed - sessionId: {}, userId: {}", sessionId, userId);
            log.debug("Refreshed tokens for session: {} user: {}", sessionId, userId);
        } catch (JOSEException e) {
            log.error("Error refreshing tokens for user: {}", userId, e);
            throw new TokenGenerationException("Failed to refresh tokens for user: " + userId, e);
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

            List<UserSession> sessionsToRevoke = new ArrayList<>();
            Instant now = Instant.now();

            for (int i = 0; i < sessionsToRemove; i++) {
                UserSession oldestSession = sessions.get(i);
                oldestSession.setRevoked(true);
                oldestSession.setRevokedAt(now);
                sessionsToRevoke.add(oldestSession);
                log.info("Security audit: Revoked oldest session: {} for user: {}", oldestSession.getSessionId(), userId);
            }

            // Batch save for better performance
            repository.saveAll(sessionsToRevoke);
        }
    }

    private JWTPayloadRecord createPayloadRecord(String jti, String sub, Instant iat, Instant exp, Set<String> authorities, TokenType type, Object... additionalClaims) {
        JWTPayloadRecord.JWTPayloadRecordBuilder builder = JWTPayloadRecord.builder().iat(iat).exp(exp).type(type.name()).additionalClaims(additionalClaims);
        if (jti != null) builder.jti(jti);
        if (sub != null) builder.sub(sub);
        if (authorities != null) builder.authorities(authorities);
        return builder.build();
    }

    @Override
    @Transactional
    public boolean revokeSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            log.warn("Attempted to revoke session with null or empty sessionId");
            return false;
        }

        Optional<UserSession> sessionOpt = repository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("Session not found for revocation: {}", sessionId);
            return false;
        }

        UserSession session = sessionOpt.get();
        if (session.isRevoked()) {
            log.debug("Session already revoked: {}", sessionId);
            return false;
        }

        session.setRevoked(true);
        session.setRevokedAt(Instant.now());
        repository.save(session);

        log.info("Security audit: Session revoked - sessionId: {}, userId: {}", sessionId, session.getUserId());
        return true;
    }

    @Override
    @Transactional
    public int revokeAllUserSessions(String userId) {
        if (userId == null || userId.isBlank()) {
            log.warn("Attempted to revoke sessions with null or empty userId");
            return 0;
        }

        List<UserSession> activeSessions = repository.findByUserIdAndRevokedFalse(userId);
        if (activeSessions.isEmpty()) {
            log.debug("No active sessions found for user: {}", userId);
            return 0;
        }

        Instant now = Instant.now();
        activeSessions.forEach(session -> {
            session.setRevoked(true);
            session.setRevokedAt(now);
        });

        repository.saveAll(activeSessions);
        int revokedCount = activeSessions.size();

        log.info("Security audit: All sessions revoked - userId: {}, count: {}", userId, revokedCount);
        return revokedCount;
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

    private Set<String> extractGuestPermissions(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return Set.of();
        }
        if (claims.size() > 1 || !claims.containsKey(PERMISSIONS_CLAIM)) {
            throw new TokenGenerationException("Only the 'permissions' claim is allowed for guest tokens");
        }
        Object rawPermissions = claims.get(PERMISSIONS_CLAIM);
        if (rawPermissions == null) {
            return Set.of();
        }
        if (rawPermissions instanceof String rawString) {
            return Arrays.stream(rawString.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toSet());
        }
        if (rawPermissions instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toSet());
        }
        if (rawPermissions.getClass().isArray()) {
            return Arrays.stream((Object[]) rawPermissions)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toSet());
        }
        throw new TokenGenerationException("Unsupported 'permissions' claim type for guest token");
    }
}
