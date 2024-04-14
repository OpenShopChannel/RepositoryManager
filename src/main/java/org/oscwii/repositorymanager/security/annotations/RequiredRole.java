package org.oscwii.repositorymanager.security.annotations;

import org.oscwii.repositorymanager.model.security.Role;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface RequiredRole
{
    Role value() default Role.GUEST;
}
