package org.oscwii.repositorymanager.utils;

public class QuietException extends RuntimeException
{
    public QuietException(String message)
    {
        super(message);
    }

    public QuietException(String message, Throwable cause)
    {
        super(message, cause);
    }

    @Override
    public synchronized Throwable initCause(Throwable cause)
    {
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
}
