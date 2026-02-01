package ec.com.ecommerce.remote.security.repositories;

import ec.com.ecommerce.remote.security.entities.RemotePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RemotePermissionRepository extends JpaRepository<RemotePermissionEntity, UUID> {
    /**
     * Find permission by name
     */
    Optional<RemotePermissionEntity> findByName(String name);

    /**
     * Find all permissions by a list of permission IDs
     */
    List<RemotePermissionEntity> findAllByIdIn(Set<UUID> permissionIds);

    /**
     * Find all permissions for a user, combining:
     * - Direct permissions assigned to the user
     * - Permissions inherited from user's roles
     */
    @Query("""
            SELECT DISTINCT p FROM RemotePermissionEntity p
            WHERE p.id IN (
                /* Direct user permissions */
                SELECT ep.id.permissionId FROM EntityPermissionEntity ep
                WHERE ep.id.entityType = 'USER' AND ep.id.entityId = :userId
                UNION
                /* Permissions from user roles */
                SELECT rp.id.permissionId FROM RolePermissionEntity rp
                WHERE rp.id.roleId IN (
                    SELECT r.id FROM UserEntity u
                    JOIN u.roles r
                    WHERE u.id = :userId
                )
            )
            """)
    Set<RemotePermissionEntity> findAllPermissionsForUser(UUID userId);

    /**
     * Find direct permissions assigned to a specific entity
     */
    @Query("""
            SELECT DISTINCT p FROM RemotePermissionEntity p
            JOIN EntityPermissionEntity ep ON p.id = ep.id.permissionId
            WHERE ep.id.entityType = :entityType
            AND ep.id.entityId = :entityId
            """)
    Set<RemotePermissionEntity> findDirectPermissionsByEntity(String entityType, UUID entityId);

    /**
     * Find all permissions for a specific role
     */
    @Query("""
            SELECT DISTINCT p FROM RemotePermissionEntity p
            JOIN RolePermissionEntity rp ON p.id = rp.id.permissionId
            WHERE rp.id.roleId = :roleId
            """)
    Set<RemotePermissionEntity> findPermissionsByRole(UUID roleId);
}
