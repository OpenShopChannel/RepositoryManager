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

    public User getUser(int id)
    {
        return userDao.getById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<User> getUsers()
    {
        return userDao.getAllUsers();
    }
}
