package org.oscwii.repositorymanager.model;

public enum UpdateLevel
{
    SAME       (false),
    FIRST_RUN  (false),
    NEW_APP    (false),
    NEW_VERSION(true ),
    NEW_BINARY (true ),
    MODIFIED   (true );

    private final boolean updated;

    UpdateLevel(boolean updated)
    {
        this.updated = updated;
    }

    public boolean isUpdated()
    {
        return updated;
    }
}
