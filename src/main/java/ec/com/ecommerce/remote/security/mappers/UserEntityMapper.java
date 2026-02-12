package ec.com.ecommerce.remote.security.mappers;

import ec.com.ecommerce.remote.security.entities.EntityPermissionEntity;
import ec.com.ecommerce.remote.security.entities.RoleEntity;
import ec.com.ecommerce.remote.security.entities.UserEntity;
import ec.com.ecommerce.remote.security.projections.LoginUserProjection;
import ec.com.ecommerce.remote.security.projections.LoginUserView;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper to convert UserEntity to LoginUserProjection
 */
@Component
public class UserEntityMapper {

    /**
     * Maps UserEntity to LoginUserProjection
     *
     * @param userEntity the user entity to map
     * @return LoginUserProjection with user details
     */
    public LoginUserProjection toLoginUserProjection(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        Set<String> roles = userEntity.getRoles() != null
                ? userEntity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet())
                : Set.of();

        Set<String> permissions = userEntity.getRoles() != null
                ? userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(EntityPermissionEntity::getPermission)
                .filter(Objects::nonNull)
                .map(ec.com.ecommerce.remote.security.entities.RemotePermissionEntity::getName)
                .collect(Collectors.toSet())
                : Set.of();

        Set<String> directPermissions = userEntity.getDirectPermissions() != null
                ? userEntity.getDirectPermissions().stream()
                .map(EntityPermissionEntity::getPermission)
                .filter(Objects::nonNull)
                .map(ec.com.ecommerce.remote.security.entities.RemotePermissionEntity::getName)
                .collect(Collectors.toSet())
                : Set.of();

        return new LoginUserView(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getPasswordExpiration(),
                roles,
                permissions,
                directPermissions
        );
    }
}
