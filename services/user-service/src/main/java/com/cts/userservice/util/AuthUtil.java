package com.cts.userservice.util;

import com.cts.userservice.exception.custom.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for role and ownership based authorization checks driven by
 * gateway-injected user/role headers.
 */
@Slf4j
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    /** Returns whether the supplied role is the ADMIN role. */
    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    /** Enforces that the caller holds the required role, throwing otherwise. */
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

    /** Allows access only if the caller is an admin or owns the targeted resource. */
    public static void requireSelfOrAdmin(Long resourceUserId, Long callerUserId, String role) {
        if (isAdmin(role)) return;
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Access denied: caller {} attempted to access resources of user {}", callerUserId, resourceUserId);
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    /** Allows modification only if the caller owns the targeted resource. */
    public static void requireOwner(Long resourceUserId, Long callerUserId) {
        if (callerUserId == null || !callerUserId.equals(resourceUserId)) {
            log.warn("Access denied: caller {} attempted to modify resource of user {}", callerUserId, resourceUserId);
            throw new UnauthorizedAccessException("You can only modify your own resource");
        }
    }
}