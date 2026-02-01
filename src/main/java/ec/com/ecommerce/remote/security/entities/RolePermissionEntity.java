package ec.com.ecommerce.remote.security.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/**
 * Represents role-permission mappings from remote security database.
 * Associates permissions with roles for role-based access control.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Table(name = "remote_role_permissions")
public class RolePermissionEntity {
    @EmbeddedId
    private RolePermissionEmbeddable id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private RoleEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private RemotePermissionEntity permission;

    public RolePermissionEntity(java.util.UUID roleId, java.util.UUID permissionId) {
        this.id = new RolePermissionEmbeddable(roleId, permissionId);
    }
}
