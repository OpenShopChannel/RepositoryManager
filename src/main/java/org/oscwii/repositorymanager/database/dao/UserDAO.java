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

    @SqlUpdate("UPDATE users SET password_hash = :passwordHash WHERE id = :id")
    void updatePassword(int id, String passwordHash);

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
