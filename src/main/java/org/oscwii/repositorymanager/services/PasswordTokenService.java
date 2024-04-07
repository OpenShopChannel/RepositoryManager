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
