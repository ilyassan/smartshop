package com.smartshop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce authentication (session presence) on controller methods.
 * Automatically checks if user is logged in (has valid HTTP session).
 * Usage: @RequireAuth on any controller method that requires authentication
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {
}
