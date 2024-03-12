package org.oscwii.repositorymanager.model.app;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record PackageManifest(String name, String author, Category category,
                              String authorContact, List<String> supportedPlatforms,
                              EnumMap<Peripherals, Integer> peripherals,
                              String version, EnumSet<Flags> flags)
{
    public record Source(String type, Format format, String url, String file,
                         String userAgent, Set<String> additionalFiles)
    {
        public enum Format
        {
            SEVEN_ZIP(".7z"),
            ZIP      (".zip"),
            BZTAR    (".tar.bz2", ".tbz2"),
            GZTAR    (".tar.gz", ".tgz"),
            TAR      (".tar"),
            XZTAR    (".tar.xz", ".txz"),
            RAR      (".rar");

            private final String[] extensions;

            Format(String... extensions)
            {
                this.extensions = extensions;
            }

            public String[] extensions()
            {
                return extensions;
            }
        }
    }
}