package com.cts.orderservice.util;

import com.cts.orderservice.exception.custom.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless helpers for header-based authorization. Roles are forwarded by the API
 * gateway via the X-User-Role header; a {@link UnauthorizedAccessException} is raised
 * when the caller lacks the required role or ownership.
 */
@Slf4j
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    /**
     * Returns whether the given role is the admin role (case-insensitive).
     *
     * @param role the role to check
     * @return {@code true} if the role is ADMIN
     */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    /**
     * Asserts the caller holds the required role.
     *
     * @param actualRole   the caller's role
     * @param requiredRole the role required for the operation
     * @throws UnauthorizedAccessException if the role does not match
     */
    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            log.warn("Role check failed: required={}, actual={}", requiredRole, actualRole);
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    /**
     * Asserts the required role only when a role is present. A missing/blank role denotes a
     * trusted internal call (the gateway always injects the role for external callers).
     *
     * @param actualRole   the caller's role, possibly null/blank
     * @param requiredRole the role required for the operation
     */
    /** External calls always carry the role (gateway injects it); a missing role = trusted internal call. */
    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    /**
     * Asserts the caller is either an admin or the owner of the resource.
     *
     * @param resourceUserId the owner id of the resource
     * @param callerUserId   the caller's id
     * @param role           the caller's role
     * @throws UnauthorizedAccessException if the caller is neither admin nor owner
     */
    public static void requireSelfOrAdmin(Long resourceUserId, Long callerUserId, String role) {
        if (isAdmin(role)) return;
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Self-or-admin check failed: resourceUserId={}, callerUserId={}", resourceUserId, callerUserId);
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /**
     * Asserts the caller owns the resource.
     *
     * @param resourceUserId the owner id of the resource
     * @param callerUserId   the caller's id
     * @throws UnauthorizedAccessException if the caller is not the owner
     */
    public static void requireOwner(Long resourceUserId, Long callerUserId) {
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Owner check failed: resourceUserId={}, callerUserId={}", resourceUserId, callerUserId);
            throw new UnauthorizedAccessException("You can only modify your own resource");
        }
    }
}