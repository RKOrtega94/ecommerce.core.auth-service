package ec.com.ecommerce.modules.auth.adapter.persistence;

import ec.com.ecommerce.modules.auth.domain.entities.UserSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing UserSession entities.
 */
@Repository
public interface UserSessionRepository extends CrudRepository<UserSession, String> {
    /**
     * Finds all active (not revoked) user sessions for a given user ID.
     *
     * @param userId the ID of the user
     * @return a list of active UserSession entities
     */
    List<UserSession> findByUserIdAndRevokedFalse(String userId);

    /**
     * Finds an active (not revoked) user session by its session ID.
     *
     * @param sessionId the session ID
     * @return an Optional containing the UserSession if found, or empty if not found
     */
    Optional<UserSession> findBySessionIdAndRevokedFalse(String sessionId);

    /**
     * Finds an active (not revoked) user session by its refresh token.
     *
     * @param refreshToken the refresh token
     * @return an Optional containing the UserSession if found, or empty if not found
     */
    Optional<UserSession> findByRefreshTokenAndRevokedFalse(String refreshToken);

    /**
     * Counts the number of active (not revoked) sessions for a given user ID.
     *
     * @param userId the ID of the user
     * @return the count of active UserSession entities
     */
    long countByUserIdAndRevokedFalse(String userId);
}
