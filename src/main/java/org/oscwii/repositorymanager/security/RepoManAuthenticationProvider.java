package org.oscwii.repositorymanager.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

public class RepoManAuthenticationProvider extends DaoAuthenticationProvider
{
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        if(!StringUtils.hasText(userDetails.getPassword()))
        {
            logger.debug("Failed to authenticate because user has no password");
            throw new BadCredentialsException("Account credentials not initialized, " +
                    "contact an Administrator for a password reset link");
        }

        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
