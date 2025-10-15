package ec.com.ecommerce.modules.auth.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "user_session", timeToLive = 86400)
public class UserSession implements Serializable {
    @Id
    private String sessionId;
    @Indexed
    private String userId;
    private Set<String> roles;
    private Set<String> permissions;
    @Indexed
    private String refreshToken;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant refreshExpiresAt;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    @Indexed
    private boolean revoked;
    private Instant revokedAt;
}
