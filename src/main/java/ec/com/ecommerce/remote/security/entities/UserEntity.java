package ec.com.ecommerce.remote.security.entities;

import ec.com.ecommerce.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "remote_users")
public class UserEntity extends BaseEntity {
    @Size(max = 50)
    @NotNull
    @Column(name = "firstname", nullable = false, length = 50)
    private String firstname;

    @Size(max = 50)
    @NotNull
    @Column(name = "lastname", nullable = false, length = 50)
    private String lastname;

    @Size(max = 50)
    @NotNull
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "password_expiration")
    private Instant passwordExpiration;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "remote_user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToMany
    @JoinColumn(name = "entity_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Filter(name = "entityTypeFilter", condition = "entity_type = 'USER'")
    private Set<EntityPermissionEntity> directPermissions = new HashSet<>();
}
