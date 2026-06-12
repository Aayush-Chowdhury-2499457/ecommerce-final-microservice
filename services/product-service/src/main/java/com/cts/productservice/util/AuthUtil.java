package com.cts.productservice.util;

import com.cts.productservice.exception.custom.UnauthorizedAccessException;
import org.springframework.stereotype.Component;

/**
 * Stateless helper for role-based authorization checks within the Product Service.
 * <p>
 * The caller's role is forwarded by the API Gateway in the {@code X-User-Role} header
 * (the gateway having already validated the JWT). These guards inspect that role and
 * raise {@link UnauthorizedAccessException} when the required privilege is missing.
 * The class is final and exposes only static members; it cannot be instantiated.
 *
 * @since 1.0
 */
public final class AuthUtil {

    /** Role name granting full administrative privileges. */
    public static final String ROLE_ADMIN = "ADMIN";

    /** Role name granting standard customer privileges. */
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    /** Prevents instantiation of this static-only utility class. */
    private AuthUtil() {}

    /**
     * Asserts that the caller holds the required role, comparing case-insensitively.
     *
     * @param actualRole   the caller's role, typically from the {@code X-User-Role} header
     * @param requiredRole the role the operation demands
     * @throws UnauthorizedAccessException if {@code actualRole} is {@code null} or does not match
     */
    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    /**
     * Asserts that the caller is either an administrator or the owner of the targeted
     * resource. Administrators bypass the ownership check.
     *
     * @param pathUserId   the user id identifying the resource being accessed
     * @param callerUserId the id of the authenticated caller
     * @param role         the caller's role
     * @throws UnauthorizedAccessException if the caller is neither an admin nor the resource owner
     */
    public static void requireSelfOrAdmin(Long pathUserId, Long callerUserId, String role) {
        if (!ROLE_ADMIN.equalsIgnoreCase(role) && (callerUserId == null || !callerUserId.equals(pathUserId))) {
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /**
     * Enforces the required role only when a role is actually present. Blank, {@code null},
     * or the literal {@code "null"} value is treated as absent and skips the check, which
     * allows trusted internal (service-to-service) calls that carry no role header.
     *
     * @param actualRole   the caller's role, possibly absent
     * @param requiredRole the role to enforce when a role is present
     * @throws UnauthorizedAccessException if a role is present but does not match {@code requiredRole}
     */
    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    /**
     * Tests whether the given role denotes an administrator, comparing case-insensitively.
     *
     * @param role the role to evaluate
     * @return {@code true} if the role is {@link #ROLE_ADMIN}, otherwise {@code false}
     */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }
}
