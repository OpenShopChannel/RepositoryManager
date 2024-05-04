/*
 * Copyright (c) 2023-2024 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

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
