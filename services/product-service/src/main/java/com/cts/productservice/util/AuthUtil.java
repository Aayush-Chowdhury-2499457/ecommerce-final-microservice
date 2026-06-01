package com.cts.productservice.util;

import com.cts.productservice.exception.custom.UnauthorizedAccessException;
import org.springframework.stereotype.Component;

@Component
public final class AuthUtil {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private AuthUtil() {}

    public static void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !actualRole.equalsIgnoreCase(requiredRole)) {
            throw new UnauthorizedAccessException("Access denied: " + requiredRole + " role required");
        }
    }

    public static void requireSelfOrAdmin(Long pathUserId, Long callerUserId, String role) {
        if (!ROLE_ADMIN.equalsIgnoreCase(role) && (callerUserId == null || !callerUserId.equals(pathUserId))) {
            throw new UnauthorizedAccessException("You can only access your own resources");
        }
    }

    public static void requireRoleIfPresent(String actualRole, String requiredRole) {
        if (actualRole != null && !actualRole.isBlank() && !actualRole.equalsIgnoreCase("null")) {
            requireRole(actualRole, requiredRole);
        }
    }

    public static boolean isAdmin(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }
}