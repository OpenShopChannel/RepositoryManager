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

package org.oscwii.repositorymanager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.spring5.EnableJdbiRepositories;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.database.dao.SettingsDAO;
import org.oscwii.repositorymanager.services.FeaturedAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@SpringBootApplication
@Controller
@EnableJdbiRepositories
@ConfigurationPropertiesScan(value = "org.oscwii.repositorymanager.config.repoman")
public class DanboApp
{
    private final FeaturedAppService featuredApp;
    private final Logger logger;
    private final RepositoryIndex index;
    private final RepoManConfig config;
    private final SettingsDAO settingsDao;

    @Autowired
    public DanboApp(FeaturedAppService featuredApp, RepositoryIndex index, RepoManConfig config, SettingsDAO settingsDao)
    {
        this.featuredApp = featuredApp;
        this.logger = LogManager.getLogger(DanboApp.class);
        this.index = index;
        this.config = config;
        this.settingsDao = settingsDao;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup()
    {
        if(settingsDao.getSetting("setup_complete").isEmpty())
            return;

        // Load the repository without updating apps
        index.initialize();
        logger.info("Indexed and serving {} apps", index.getContents().size());

        // Pick a featured app
        featuredApp.pickFeaturedApp();
    }

    @GetMapping("/")
    public Object hello(Model model)
    {
        Optional<String> gitUrl = settingsDao.getSetting("git_url");
        if(settingsDao.getSetting("setup_complete").isEmpty() || gitUrl.isEmpty())
        {
            return ResponseEntity.ok("""
           This RepositoryManager instance has not been installed.
           <a href='/setup'>Click here to go to the setup.</a>
           """);
        }

        model.addAttribute("app_count", index.getContents().size())
                .addAttribute("repository_name", index.getInfo().name())
                .addAttribute("repository_provider", index.getInfo().provider())
                .addAttribute("git_url", gitUrl.get())
                .addAttribute("base_url", config.getBaseUrl());
        return "hello_world";
    }

    public static void main(String[] args)
    {
        if(System.getProperty("spring.profiles.default") == null)
            System.setProperty("spring.profiles.default", "prod");

        SpringApplication.run(DanboApp.class, args);
    }
}
