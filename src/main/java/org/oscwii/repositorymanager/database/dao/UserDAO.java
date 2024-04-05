package org.oscwii.repositorymanager.database.dao;

import org.jdbi.v3.spring5.JdbiRepository;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.model.security.User;

import java.util.List;
import java.util.Optional;

@JdbiRepository
@RegisterConstructorMapper(User.class)
public interface UserDAO
{
    @SqlUpdate("""
            INSERT INTO users (email, username, password_hash, role)
            VALUES (:email, :username, :passwordHash, :role)
            """)
    void createUser(String username, String email, String passwordHash, Role role);

    @SqlUpdate("""
            UPDATE users
            SET enabled = :enabled,
                email = :email,
                role = :role
            WHERE id = :id
            """)
    void updateUser(int id, boolean enabled, String email, Role role);

    @SqlUpdate("DELETE FROM users WHERE username = :username")
    void deleteUser(String username);

    @SqlQuery("SELECT * FROM users WHERE email = :email")
    Optional<User> getByEmail(String email);

    @SqlQuery("SELECT * FROM users WHERE id = :id")
    Optional<User> getById(int id);

    @SqlQuery("SELECT * FROM users WHERE username = :username")
    Optional<User> getByUsername(String username);

    @SqlQuery("SELECT * FROM users")
    List<User> getAllUsers();
}
