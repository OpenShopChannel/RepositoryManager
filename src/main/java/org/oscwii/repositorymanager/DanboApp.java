package org.oscwii.repositorymanager;

import org.jdbi.v3.spring5.EnableJdbiRepositories;
import org.oscwii.repositorymanager.services.FeaturedApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
@EnableJdbiRepositories
@ConfigurationPropertiesScan(value = "org.oscwii.repositorymanager.config.repoman")
public class DanboApp
{
    @Autowired
    private RepositoryIndex index;
    @Autowired
    private FeaturedApp featuredApp;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup()
    {
        // Load the repository without updating apps
        index.initialize();

        // Pick a featured app
        featuredApp.pickFeaturedApp();
    }

    @GetMapping("/")
    public String hello(Model model)
    {
        return "hello_world";
    }

    // TODO remove debug
    @GetMapping("/debug")
    @ResponseBody
    public String debug()
    {
        index.index(true);
        return "running";
    }

    public static void main(String[] args)
    {
        SpringApplication.run(DanboApp.class, args);
    }
}
