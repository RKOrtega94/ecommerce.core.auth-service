package ec.com.ecommerce.remote.security.entities;

import ec.com.ecommerce.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Table(name = "remote_permissions")
public class RemotePermissionEntity extends BaseEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;
}
