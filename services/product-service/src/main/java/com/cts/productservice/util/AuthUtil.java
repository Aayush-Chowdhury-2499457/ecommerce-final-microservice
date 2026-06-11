package com.cts.productservice.util;

import com.cts.productservice.exception.custom.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Static helpers for role-based authorization checks against the gateway-supplied role header.
 */
@Slf4j
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    /** Throws {@link UnauthorizedAccessException} unless the actual role matches the required role. */
    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            log.warn("Access denied: required role {} but got {}", requiredRole, actualRole);
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    /** Allows access only to an admin or the resource owner; throws otherwise. */
    public static void requireSelfOrAdmin(Long pathUserId, Long callerUserId, String role) {
        if (!ROLE_ADMIN.equalsIgnoreCase(role) && (callerUserId == null || !callerUserId.equals(pathUserId))) {
            log.warn("Access denied: caller {} cannot access resources of {}", callerUserId, pathUserId);
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /** Enforces the required role only when a (non-blank, non-"null") role header is present. */
    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    /** Returns {@code true} if the given role is the admin role. */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }
}