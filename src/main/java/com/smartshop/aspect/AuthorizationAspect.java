package com.smartshop.aspect;

import com.smartshop.annotation.RequireRole;
import com.smartshop.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect for role-based authorization.
 * Automatically checks @RequireRole annotation on controller methods.
 */
@Aspect
@Component
@Slf4j
public class AuthorizationAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        String requiredRole = requireRole.value();

        // Get the current HTTP session
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            log.warn("Unable to get request attributes for method: {}", joinPoint.getSignature());
            throw new UnauthorizedException("Not authenticated");
        }

        HttpSession session = attrs.getRequest().getSession(false);
        if (session == null) {
            log.warn("No session found - user not authenticated for method: {}", joinPoint.getSignature());
            throw new UnauthorizedException("Please login first");
        }

        Object userId = session.getAttribute("LOGGED_IN_USER");
        if (userId == null) {
            log.warn("No LOGGED_IN_USER in session for method: {}", joinPoint.getSignature());
            throw new UnauthorizedException("Please login first");
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals(requiredRole)) {
            log.warn("Unauthorized access attempt: Required role '{}' but user has '{}' - Method: {}",
                    requiredRole, userRole, joinPoint.getSignature());
            throw new UnauthorizedException("Only " + requiredRole.toLowerCase() + "s can access this resource");
        }

        log.debug("Authorization check passed for role: {} - Method: {}", requiredRole, joinPoint.getSignature());
    }
}
