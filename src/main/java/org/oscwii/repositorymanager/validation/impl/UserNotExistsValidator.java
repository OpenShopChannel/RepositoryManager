package org.oscwii.repositorymanager.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.oscwii.repositorymanager.controllers.admin.SecurityController.UserForm;
import org.oscwii.repositorymanager.services.AuthService;
import org.oscwii.repositorymanager.validation.UserNotExists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserNotExistsValidator implements ConstraintValidator<UserNotExists, UserForm>
{
    private final AuthService authService;

    @Autowired
    public UserNotExistsValidator(AuthService authService)
    {
        this.authService = authService;
    }

    @Override
    public boolean isValid(UserForm form, ConstraintValidatorContext context)
    {
        if(authService.userExists(form.username()))
            return false;
        return !authService.isEmailInUse(form.email());
    }
}
