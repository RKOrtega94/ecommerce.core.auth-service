package ec.com.ecommerce.remote.security.entities;

import ec.com.ecommerce.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "remote_roles")
public class RoleEntity extends BaseEntity {
    private String name;

    @OneToMany
    @Filter(name = "entityTypeFilter", condition = "entity_type = 'ROLE'")
    @JoinColumn(name = "entity_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<EntityPermissionEntity> permissions = new HashSet<>();
}
