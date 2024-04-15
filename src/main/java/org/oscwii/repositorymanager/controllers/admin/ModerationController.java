package org.oscwii.repositorymanager.controllers.admin;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.factory.DiscordWebhookFactory;
import org.oscwii.repositorymanager.model.ModeratedBinary;
import org.oscwii.repositorymanager.model.ModeratedBinary.Status;
import org.oscwii.repositorymanager.model.security.Role;
import org.oscwii.repositorymanager.model.security.User;
import org.oscwii.repositorymanager.security.annotations.RequiredRole;
import org.oscwii.repositorymanager.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.Optional;

@Controller
@RequiredRole(Role.MODERATOR)
@RequestMapping(path = "/admin/moderation")
public class ModerationController extends BaseAdminController
{
    private final Logger logger = LogManager.getLogger("Moderation");

    @Autowired
    private DiscordWebhookFactory discordWebhook;

    @GetMapping
    public String moderation(Model model)
    {
        model.addAttribute("modEntries", modDao.getAllEntries());
        return "admin/moderation";
    }

    @GetMapping("/{checksum}/{action}")
    public Object action(@PathVariable String checksum, @PathVariable String action, HttpServletRequest request, RedirectAttributes attributes)
    {
        Optional<ModeratedBinary> optEntry = modDao.findByChecksum(checksum);
        if(optEntry.isEmpty())
            return ResponseEntity.notFound().build();

        ModeratedBinary entry = optEntry.get();
        switch(action)
        {
            case "approve":
                approveApp(entry, getUser(request));
                attributes.addFlashAttribute("message", "success:Approved " + entry.app() + "-" + checksum);
                break;
            case "reject":
                rejectApp(entry, getUser(request));
                attributes.addFlashAttribute("message", "danger:Rejected " + entry.app() + "-" + checksum);
                break;
            case "download":
                return FileUtil.getContent(Path.of("data", "moderation", checksum + ".zip"));
        }

        return "redirect:/admin/moderation";
    }

    private void approveApp(ModeratedBinary entry, User moderator)
    {
        String checksum = entry.checksum();
        modDao.updateEntry(checksum, Status.APPROVED, moderator.getId());
        entry = modDao.findByChecksum(checksum).orElseThrow();
        notifyDiscord(entry, Status.APPROVED);
        logger.info("App {} ({}) has been APPROVED by {}", entry.app(), checksum, moderator.getUsername());
    }

    private void rejectApp(ModeratedBinary entry, User moderator)
    {
        String checksum = entry.checksum();
        modDao.updateEntry(checksum, Status.REJECTED, moderator.getId());
        entry = modDao.findByChecksum(checksum).orElseThrow();
        notifyDiscord(entry, Status.REJECTED);
        logger.info("App {} ({}) has been REJECTED by {}", entry.app(), checksum, moderator.getUsername());
    }

    private void notifyDiscord(ModeratedBinary entry, Status status)
    {
        try(WebhookClient webhook = discordWebhook.modWebhook())
        {
            if(webhook == null)
                return;

            WebhookEmbed embed = new WebhookEmbedBuilder()
                    .setTitle(new WebhookEmbed.EmbedTitle(status == Status.APPROVED ?
                            "Version approved by moderation" : "Version rejected by moderation", null))
                    .setDescription(
                            entry.app() + "-" + entry.checksum() + "\n" +
                                (status == Status.APPROVED ?
                                    "This application will be available for download starting with the next re-index." :
                                    "Consider removing " + entry.app() + ".oscmeta from the repository."
                                ))
                    .build();
            webhook.send(embed);
        }
    }
}
