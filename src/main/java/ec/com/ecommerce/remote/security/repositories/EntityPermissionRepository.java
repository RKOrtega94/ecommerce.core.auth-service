package ec.com.ecommerce.remote.security.repositories;

import ec.com.ecommerce.remote.security.entities.EntityPermissionEmbeddable;
import ec.com.ecommerce.remote.security.entities.EntityPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EntityPermissionRepository extends JpaRepository<EntityPermissionEntity, EntityPermissionEmbeddable> {
    /**
     * Find all direct permissions assigned to a specific entity
     */
    @Query("""
            SELECT ep FROM EntityPermissionEntity ep
            WHERE ep.id.entityType = :entityType
            AND ep.id.entityId = :entityId
            """)
    Set<EntityPermissionEntity> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Find all direct permissions for a user (entity_type = 'USER')
     */
    @Query("""
            SELECT ep FROM EntityPermissionEntity ep
            WHERE ep.id.entityType = 'USER'
            AND ep.id.entityId = :userId
            """)
    Set<EntityPermissionEntity> findByUserId(UUID userId);

    /**
     * Find all permissions by permission ID
     */
    @Query("""
            SELECT ep FROM EntityPermissionEntity ep
            WHERE ep.id.permissionId = :permissionId
            """)
    List<EntityPermissionEntity> findByPermissionId(UUID permissionId);

    /**
     * Check if user has a specific direct permission
     */
    @Query("""
            SELECT CASE WHEN COUNT(ep) > 0 THEN true ELSE false END
            FROM EntityPermissionEntity ep
            WHERE ep.id.entityType = 'USER'
            AND ep.id.entityId = :userId
            AND ep.id.permissionId = :permissionId
            """)
    boolean userHasDirectPermission(UUID userId, UUID permissionId);
}
