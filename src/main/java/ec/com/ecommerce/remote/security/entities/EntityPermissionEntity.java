package ec.com.ecommerce.remote.security.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/**
 * Represents direct entity permissions mapping from entity_permissions foreign table.
 * Maps permissions assigned directly to entities (users, groups, etc.).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Table(name = "entity_permissions_foreing")
public class EntityPermissionEntity {
    @EmbeddedId
    private EntityPermissionEmbeddable id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private RemotePermissionEntity permission;

    public EntityPermissionEntity(String entityType, java.util.UUID entityId, java.util.UUID permissionId) {
        this.id = new EntityPermissionEmbeddable(entityType, entityId, permissionId);
    }
}
