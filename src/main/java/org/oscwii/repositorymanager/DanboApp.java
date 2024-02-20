package org.oscwii.repositorymanager;

import org.jdbi.v3.spring5.EnableJdbiRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
@EnableJdbiRepositories
public class DanboApp
{
    @GetMapping("/")
    public String hello(Model model)
    {
        return "hello_world";
    }

    public static void main(String[] args)
    {
        SpringApplication.run(DanboApp.class, args);
    }
}
