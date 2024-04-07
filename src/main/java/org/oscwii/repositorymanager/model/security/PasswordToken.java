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
