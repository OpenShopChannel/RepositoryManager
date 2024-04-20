package org.oscwii.repositorymanager.config.repoman;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "repository-manager.shop")
public record ShopConfig(boolean generateBanner,
                         Path bannerGeneratorPath,
                         Path bannerOutputPath)
{
}
