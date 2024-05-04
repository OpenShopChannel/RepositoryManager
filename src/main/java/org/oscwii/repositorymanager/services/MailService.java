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

package org.oscwii.repositorymanager.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.model.security.PasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class MailService
{
    private final Logger logger;
    private final RepoManConfig config;

    @Autowired
    public MailService(RepoManConfig config)
    {
        this.logger = LogManager.getLogger(MailService.class);
        this.config = config;
    }

    public void sendPasswordReset(String email, PasswordToken token)
    {
        Email from = new Email(config.mailConfig.senderAddress(), "Repository Manager");
        Email to = new Email(email);

        String subject = "Repository Manager Password Reset";
        Content content = new Content(MediaType.TEXT_HTML_VALUE,
                MAIL_CONTENT.formatted(config.getBaseUrl(), token.getToken()));
        Mail mail = new Mail(from, subject, to, content);
        send(mail);
    }

    private void send(Mail mail)
    {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        send0(mail, request);
    }

    private void send0(Mail mail, Request request)
    {
        try
        {
            SendGrid client = new SendGrid(config.mailConfig.sendGridApiKey());
            request.setBody(mail.build());
            Response res = client.api(request);

            if(res.getStatusCode() != 202)
                logger.error("Failed to send email: {}", res.getBody());
        }
        catch(Exception e)
        {
            logger.error("Failed to send email", e);
        }
    }

    private static final String MAIL_CONTENT = """
            An Administrator has requested a password reset for your account.<br>
            Please use the following link to reset your password:<br>
            <a href="%s/admin/reset-password?token=%s">Reset Password</a><br>
            <br>
            This link will expire in 1 hour.
            <br>
            Repository Manager
            """;
}
