package com.cts.reviewservice.util;

import com.cts.reviewservice.exception.custom.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless helper for role-based and ownership authorization checks.
 */
@Slf4j
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    /** Returns whether the given role is the ADMIN role. */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    /** Asserts the caller's role matches the required role, else throws. */
    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            log.warn("Access denied: required role {} but caller role was {}", requiredRole, actualRole);
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    /** External calls always carry the role (gateway injects it); a missing role = trusted internal call. */
    /** Enforces the role only when one is present; absence implies a trusted internal call. */
    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    /** Allows access when the caller is an admin or owns the resource, else throws. */
    public static void requireSelfOrAdmin(Long resourceUserId, Long callerUserId, String role) {
        if (isAdmin(role)) return;
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Access denied: caller {} is neither owner ({}) nor admin", callerUserId, resourceUserId);
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /** Allows access only when the caller owns the resource, else throws. */
    public static void requireOwner(Long resourceUserId, Long callerUserId) {
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Access denied: caller {} is not owner ({})", callerUserId, resourceUserId);
            throw new UnauthorizedAccessException("You can only modify your own resource");
        }
    }
}