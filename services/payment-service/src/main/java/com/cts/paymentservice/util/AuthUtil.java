package com.cts.paymentservice.util;

import com.cts.paymentservice.exception.custom.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless helper for header-based role and ownership authorization checks.
 */
@Slf4j
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    /**
     * Returns whether the given role is the admin role (case-insensitive).
     *
     * @param role the role to test
     * @return {@code true} if the role is {@code ADMIN}
     */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    /**
     * Requires the caller's role to match the expected role.
     *
     * @param actualRole   the caller's role
     * @param requiredRole the role required for the operation
     * @throws UnauthorizedAccessException if the roles do not match
     */
    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            log.warn("Access denied: required role={}, actual role={}", requiredRole, actualRole);
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    /**
     * Enforces {@code requireRole} only when a role is present; a missing role is treated
     * as a trusted internal call.
     *
     * @param actualRole   the caller's role, possibly absent
     * @param requiredRole the role required when one is present
     */
    /** External calls always carry the role (gateway injects it); a missing role = trusted internal call. */
    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    /**
     * Allows access if the caller is an admin or owns the resource.
     *
     * @param resourceUserId the owner id of the resource
     * @param callerUserId   the caller's id
     * @param role           the caller's role
     * @throws UnauthorizedAccessException if the caller is neither admin nor owner
     */
    public static void requireSelfOrAdmin(Long resourceUserId, Long callerUserId, String role) {
        if (isAdmin(role)) return;
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Access denied: caller={} is not owner={} and not admin", callerUserId, resourceUserId);
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /**
     * Requires the caller to be the owner of the resource.
     *
     * @param resourceUserId the owner id of the resource
     * @param callerUserId   the caller's id
     * @throws UnauthorizedAccessException if the caller does not own the resource
     */
    public static void requireOwner(Long resourceUserId, Long callerUserId) {
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            throw new UnauthorizedAccessException("You can only modify your own resource");
        }
    }
}