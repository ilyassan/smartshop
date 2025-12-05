package com.smartshop.aspect;

import com.smartshop.annotation.RequireAuth;
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
 * AOP Aspect for authentication verification.
 * Automatically checks @RequireAuth annotation on controller methods.
 * Ensures user has a valid HTTP session (is logged in).
 */
@Aspect
@Component
@Slf4j
public class AuthenticationAspect {

    @Before("@annotation(requireAuth)")
    public void checkAuthentication(JoinPoint joinPoint, RequireAuth requireAuth) {
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

        log.debug("Authentication check passed for user: {} - Method: {}", userId, joinPoint.getSignature());
    }
}
