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

package org.oscwii.repositorymanager.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.database.dao.UserDAO;
import org.oscwii.repositorymanager.model.security.PasswordToken;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.model.security.DummyUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService implements UserDetailsManager
{
    private final Logger logger;
    private final MailService mailService;
    private final PasswordEncoder encoder;
    private final PasswordTokenService passwordTokenService;
    private final UserDAO userDao;

    @Autowired
    public AuthService(MailService mailService, PasswordEncoder encoder, PasswordTokenService passwordTokenService, UserDAO userDao)
    {
        this.logger = LogManager.getLogger(AuthService.class);
        this.mailService = mailService;
        this.encoder = encoder;
        this.passwordTokenService = passwordTokenService;
        this.userDao = userDao;
    }

    @Override
    public void createUser(UserDetails details)
    {
        DummyUser user = (DummyUser) details;

        if(userExists(user.getUsername()))
            throw new IllegalArgumentException("User already exists");
        else if(isEmailInUse(user.getEmail()))
            throw new IllegalArgumentException("Email is already in use");

        String password = null;
        if(user.getPassword() != null)
            password = encoder.encode(user.getPassword());

        userDao.createUser(user.getUsername(), user.getEmail(), password, user.getRole());
        logger.info("Created new user {} with role {}", user.getUsername(), user.getRole());
    }

    @Override
    public void updateUser(UserDetails details)
    {
        User user = (User) details;
        userDao.updateUser(user.getId(), user.isEnabled(), user.getEmail(), user.getRole());
        logger.info("User {} ({}) has been updated", user.getUsername(), user.getRole());
    }

    @Override
    public void deleteUser(String username)
    {
        userDao.deleteUser(username);
        logger.info("User {} has been deleted", username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {}

    public void changePassword(int id, String password)
    {
        password = encoder.encode(password);
        userDao.updatePassword(id, password);
        logger.info("Password has been updated for {}", getUser(id).getUsername());
    }

    public void requestPasswordReset(User user)
    {
        PasswordToken token = passwordTokenService.createToken(user.getId());
        mailService.sendPasswordReset(user.getEmail(), token);
        logger.info("Password reset link has been requested for {}", user.getUsername());
    }

    public PasswordToken getPasswordToken(String tokenStr)
    {
        return passwordTokenService.consumeToken(tokenStr);
    }

    public void validatePasswordToken(String tokenStr)
    {
        PasswordToken token = passwordTokenService.getToken(tokenStr);
        if(token == null)
            throw new IllegalArgumentException("Invalid password reset link");
        if(token.isExpired())
            throw new IllegalArgumentException("This password reset link has expired");
    }

    @Override
    public boolean userExists(String username)
    {
        return userDao.getByUsername(username).isPresent();
    }

    public boolean isEmailInUse(String email)
    {
        return userDao.getByEmail(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        return userDao.getByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUser(int id)
    {
        return userDao.getById(id).orElse(null);
    }

    public List<User> getUsers()
    {
        return userDao.getAllUsers();
    }
}
