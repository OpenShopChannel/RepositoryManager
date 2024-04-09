package org.oscwii.repositorymanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.oscwii.repositorymanager.validation.impl.UserNotExistsValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = UserNotExistsValidator.class)
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
public @interface UserNotExists
{
    String message() default "Username or email already in use";

    Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
