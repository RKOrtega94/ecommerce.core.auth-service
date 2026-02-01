package ec.com.ecommerce.remote.security.repositories;

import ec.com.ecommerce.remote.security.entities.RolePermissionEmbeddable;
import ec.com.ecommerce.remote.security.entities.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionEmbeddable> {
    /**
     * Find all permissions assigned to a specific role
     */
    @Query("""
            SELECT rp FROM RolePermissionEntity rp
            WHERE rp.id.roleId = :roleId
            """)
    Set<RolePermissionEntity> findByRoleId(UUID roleId);

    /**
     * Find all role-permission mappings for multiple roles
     */
    @Query("""
            SELECT rp FROM RolePermissionEntity rp
            WHERE rp.id.roleId IN :roleIds
            """)
    Set<RolePermissionEntity> findByRoleIdIn(Set<UUID> roleIds);

    /**
     * Find all roles that have a specific permission
     */
    @Query("""
            SELECT rp FROM RolePermissionEntity rp
            WHERE rp.id.permissionId = :permissionId
            """)
    List<RolePermissionEntity> findByPermissionId(UUID permissionId);

    /**
     * Check if a role has a specific permission
     */
    @Query("""
            SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END
            FROM RolePermissionEntity rp
            WHERE rp.id.roleId = :roleId
            AND rp.id.permissionId = :permissionId
            """)
    boolean roleHasPermission(UUID roleId, UUID permissionId);
}
