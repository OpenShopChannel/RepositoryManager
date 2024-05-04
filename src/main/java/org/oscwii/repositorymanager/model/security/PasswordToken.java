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

package org.oscwii.repositorymanager.model.security;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class PasswordToken
{
    private final Date expiryDate;
    private final int id;
    private final String token;

    public PasswordToken(int id)
    {
        this.expiryDate = Date.from(Instant.now().plusSeconds(EXPIRATION));
        this.id = id;
        this.token = UUID.randomUUID().toString().replace("-", "");
    }

    public int getId()
    {
        return id;
    }

    public String getToken()
    {
        return token;
    }

    public boolean isExpired()
    {
        return Calendar.getInstance().after(expiryDate);
    }

    private static final int EXPIRATION = 60 * 60;
}
