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

package org.oscwii.repositorymanager.treatments;

import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.RepositoryIndex;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;

import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseTreatmentRunnable implements TreatmentRunnable
{
    private final String id;

    protected final Logger logger;

    protected BaseTreatmentRunnable(String id)
    {
        this.logger = RepositoryIndex.LOGGER;
        this.id = id;
    }

    @Override
    public abstract void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException;

    @Override
    public String getId()
    {
        return id;
    }

    protected void checkAllowedPath(Path workingDir, Path attempted)
    {
        attempted = attempted.toAbsolutePath();
        if(!attempted.toAbsolutePath().startsWith(workingDir))
        {
            throw new IllegalArgumentException("ILLEGAL ACCESS! Treatment tried to modify a prohibited path: " +
                    attempted);
        }
    }
}
