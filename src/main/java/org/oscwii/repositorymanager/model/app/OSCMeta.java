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

package org.oscwii.repositorymanager.model.app;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record OSCMeta(String name, String author, String category,
                      String authorContact, List<String> supportedPlatforms,
                      List<String> peripherals, String version, EnumSet<Flag> flags,
                      Source source, List<Treatment> treatments)
{
    public record Source(String type, Format format, String url, String file,
                         String userAgent, Set<String> additionalFiles)
    {
        public enum Format
        {
            @SerializedName("7z")
            SEVEN_ZIP("7z"),
            @SerializedName("zip")
            ZIP      ("zip"),
            @SerializedName("bztar")
            BZTAR    ("tar.bz2", "tbz2"),
            @SerializedName("gztar")
            GZTAR    ("tar.gz", "tgz"),
            @SerializedName("tar")
            TAR      ("tar"),
            @SerializedName("xztar")
            XZTAR    ("tar.xz", "txz"),
            @SerializedName("rar")
            RAR      ("rar");

            private final String[] extensions;

            Format(String... extensions)
            {
                this.extensions = extensions;
            }

            @Nullable
            public static Format fromExtension(String extension)
            {
                for(Format format : values())
                {
                    for(String ext : format.extensions)
                    {
                        if(ext.equals(extension))
                            return format;
                    }
                }

                return null;
            }
        }
    }

    public record Treatment(@SerializedName("treatment") String id, String[] arguments)
    {
        @Override
        public String toString()
        {
            return "Treatment{" +
                    "id='" + id + '\'' +
                    ", arguments=" + Arrays.toString(arguments) +
                    '}';
        }
    }

    public enum Flag
    {
        @SerializedName("writes_to_nand")
        WRITES_TO_NAND
    }
}
