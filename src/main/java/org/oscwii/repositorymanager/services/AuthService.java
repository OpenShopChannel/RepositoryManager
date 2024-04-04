package org.oscwii.repositorymanager.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.database.dao.UserDAO;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.model.security.UserForm;
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
    private final PasswordEncoder encoder;
    private final UserDAO userDao;

    @Autowired
    public AuthService(PasswordEncoder encoder, UserDAO userDao)
    {
        this.logger = LogManager.getLogger(AuthService.class);
        this.encoder = encoder;
        this.userDao = userDao;
    }

    @Override
    public void createUser(UserDetails details)
    {
        UserForm user = (UserForm) details;
        String password = encoder.encode(details.getPassword());
        userDao.createUser(user.getUsername(), user.getEmail(), password, user.getRole());
        logger.info("Created new user {} with role {}", user.getUsername(), user.getRole());
    }

    @Override
    public void updateUser(UserDetails user)
    {

    }

    @Override
    public void deleteUser(String username)
    {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword)
    {

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

    public List<User> getUsers()
    {
        return userDao.getAllUsers();
    }
}
