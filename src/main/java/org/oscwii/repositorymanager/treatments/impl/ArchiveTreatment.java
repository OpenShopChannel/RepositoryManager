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

package org.oscwii.repositorymanager.treatments.impl;

import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta.Source.Format;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;
import org.oscwii.repositorymanager.treatments.BaseTreatmentRunnable;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class ArchiveTreatment
{
    @Autowired
    private ArchiveTreatment(TreatmentRegistry registry)
    {
        registry.registerTreatment(new Extract());
    }

    public static class Extract extends BaseTreatmentRunnable
    {
        private Extract()
        {
            super("archive.extract");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            String[] arguments = treatment.arguments();
            Assert.isTrue(arguments.length >= 2, "Extract treatment requires two arguments!");

            Path archive = workingDir.resolve(arguments[0]);
            Path destination = workingDir.resolve(arguments[1]);
            checkAllowedPath(workingDir, archive);
            checkAllowedPath(workingDir, destination);

            String formatRaw = archive.toFile().getName().split("\\.", 2)[1];
            Format format = Format.fromExtension(formatRaw);
            Assert.notNull(format, "Unsupported archive format: " + formatRaw);
            FileUtil.extractArchive(archive.toFile(), format, destination);
            logger.info("  - Extracted {} to {}", arguments[0], arguments[1]);
        }
    }
}
