package ec.com.ecommerce.remote.security.services;

import ec.com.ecommerce.remote.security.entities.RemotePermissionEntity;
import ec.com.ecommerce.remote.security.entities.UserEntity;
import ec.com.ecommerce.remote.security.mappers.UserEntityMapper;
import ec.com.ecommerce.remote.security.projections.LoginUserProjection;
import ec.com.ecommerce.remote.security.repositories.EntityPermissionRepository;
import ec.com.ecommerce.remote.security.repositories.RemotePermissionRepository;
import ec.com.ecommerce.remote.security.repositories.RolePermissionRepository;
import ec.com.ecommerce.remote.security.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service to manage and retrieve user permissions from multiple sources:
 * - Direct entity permissions (assigned directly to user)
 * - Role-based permissions (inherited from user's roles)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPermissionService {

    private final UserRepository userRepository;
    private final RemotePermissionRepository remotePermissionRepository;
    private final EntityPermissionRepository entityPermissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserEntityMapper userEntityMapper;

    /**
     * Get all permissions for a user combining direct and role-based permissions
     *
     * @param userId The user ID
     * @return Set of all permissions for the user
     */
    public Set<RemotePermissionEntity> getUserAllPermissions(UUID userId) {
        log.debug("Fetching all permissions for user: {}", userId);
        return remotePermissionRepository.findAllPermissionsForUser(userId);
    }

    /**
     * Get user with all associated roles and direct permissions
     *
     * @param userId The user ID
     * @return Optional containing UserEntity with eager-loaded roles and permissions
     */
    public Optional<UserEntity> getUserWithPermissions(UUID userId) {
        log.debug("Fetching user with permissions: {}", userId);
        return userRepository.findByIdWithPermissions(userId);
    }

    /**
     * Get user by username with all associated roles and direct permissions
     *
     * @param username The username
     * @return Optional containing LoginUserProjection with user details
     */
    public Optional<LoginUserProjection> getUserWithPermissionsByUsername(String username) {
        log.debug("Fetching user with permissions by username: {}", username);
        return userRepository.findByUsername(username)
                .map(userEntityMapper::toLoginUserProjection);
    }

    /**
     * Get direct permissions for a user
     *
     * @param userId The user ID
     * @return Set of direct permissions
     */
    public Set<RemotePermissionEntity> getUserDirectPermissions(UUID userId) {
        log.debug("Fetching direct permissions for user: {}", userId);
        return remotePermissionRepository.findDirectPermissionsByEntity("USER", userId);
    }

    /**
     * Get permissions inherited from user's roles
     *
     * @param userId The user ID
     * @return Set of role-based permissions
     */
    public Set<RemotePermissionEntity> getUserRolePermissions(UUID userId) {
        return null;
    }

    /**
     * Get permissions for a specific role
     *
     * @param roleId The role ID
     * @return Set of permissions for the role
     */
    public Set<RemotePermissionEntity> getRolePermissions(UUID roleId) {
        log.debug("Fetching permissions for role: {}", roleId);
        return remotePermissionRepository.findPermissionsByRole(roleId);
    }

    /**
     * Check if user has a specific permission (direct or role-based)
     *
     * @param userId       The user ID
     * @param permissionId The permission ID
     * @return true if user has permission, false otherwise
     */
    public boolean userHasPermission(UUID userId, UUID permissionId) {
        log.debug("Checking if user {} has permission {}", userId, permissionId);

        // Check direct permission
        if (entityPermissionRepository.userHasDirectPermission(userId, permissionId)) {
            return true;
        }

        // Check role-based permissions
        return userRepository.findByIdWithPermissions(userId).map(user -> user.getRoles().stream().anyMatch(role -> rolePermissionRepository.roleHasPermission(role.getId(), permissionId))).orElse(false);
    }

    /**
     * Check if user has any of the specified permissions
     *
     * @param userId        The user ID
     * @param permissionIds Set of permission IDs to check
     * @return true if user has at least one of the permissions
     */
    public boolean userHasAnyPermission(UUID userId, Set<UUID> permissionIds) {
        log.debug("Checking if user {} has any permission from: {}", userId, permissionIds);
        return permissionIds.stream().anyMatch(permissionId -> userHasPermission(userId, permissionId));
    }

    /**
     * Check if user has all of the specified permissions
     *
     * @param userId        The user ID
     * @param permissionIds Set of permission IDs to check
     * @return true if user has all the permissions
     */
    public boolean userHasAllPermissions(UUID userId, Set<UUID> permissionIds) {
        log.debug("Checking if user {} has all permissions from: {}", userId, permissionIds);
        return permissionIds.stream().allMatch(permissionId -> userHasPermission(userId, permissionId));
    }
}
