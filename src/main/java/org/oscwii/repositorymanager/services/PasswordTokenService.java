/*
 * Copyright (c) 2023-2025 Open Shop Channel
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

import org.oscwii.repositorymanager.model.security.PasswordToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class PasswordTokenService
{
    private final List<PasswordToken> tokens;

    public PasswordTokenService()
    {
        this.tokens = new ArrayList<>();
    }

    public PasswordToken createToken(int id)
    {
        PasswordToken token = new PasswordToken(id);
        tokens.add(token);
        return token;
    }

    public PasswordToken getToken(String token)
    {
        for(PasswordToken t : tokens)
        {
            if(t.getToken().equals(token))
                return t;
        }

        return null;
    }

    public PasswordToken consumeToken(String token)
    {
        Iterator<PasswordToken> iterator = tokens.iterator();
        while(iterator.hasNext())
        {
            PasswordToken t = iterator.next();
            if(t.getToken().equals(token))
            {
                iterator.remove();
                return t;
            }
        }

        return null;
    }
}
