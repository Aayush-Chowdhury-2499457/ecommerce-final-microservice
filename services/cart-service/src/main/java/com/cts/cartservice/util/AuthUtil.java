package com.cts.cartservice.util;

import com.cts.cartservice.exception.custom.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for role-based authorization checks on incoming requests.
 */
@Slf4j
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    /** Returns {@code true} if the given role is the admin role. */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    /** Asserts that the actual role matches the required role, else throws. */
    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            log.warn("Access denied: required role {} but caller role was {}", requiredRole, actualRole);
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    /** External calls always carry the role (gateway injects it); a missing role = trusted internal call. */
    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    /** Allows the call only if the caller is an admin or owns the resource. */
    public static void requireSelfOrAdmin(Long resourceUserId, Long callerUserId, String role) {
        if (isAdmin(role)) return;
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /** Allows the call only if the caller owns the resource. */
    public static void requireOwner(Long resourceUserId, Long callerUserId) {
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            throw new UnauthorizedAccessException("You can only modify your own resource");
        }
    }
}