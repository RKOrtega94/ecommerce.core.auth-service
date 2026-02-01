package ec.com.ecommerce.remote.security.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EntityPermissionEmbeddable implements Serializable {
    @Column(name = "entity_type", nullable = false, length = 20)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;
}
