package ec.com.ecommerce.remote.security.repositories;

import ec.com.ecommerce.remote.security.entities.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    /**
     * Find a user by username with eager loading of roles and direct permissions
     */
    @Query("""
            SELECT DISTINCT u FROM UserEntity u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions rp
            LEFT JOIN FETCH u.directPermissions dp
            WHERE u.username = :username
            """)
    Optional<UserEntity> findByUsername(@NotNull @NotEmpty @NotBlank String username);

    /**
     * Find a user by ID with eager loading of roles and direct permissions
     */
    @Query("""
            SELECT DISTINCT u FROM UserEntity u
            LEFT JOIN FETCH u.roles
            LEFT JOIN FETCH u.directPermissions
            WHERE u.id = :userId
            """)
    Optional<UserEntity> findByIdWithPermissions(UUID userId);
}