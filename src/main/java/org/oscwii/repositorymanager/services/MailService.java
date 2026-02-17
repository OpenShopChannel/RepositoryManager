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

package org.oscwii.repositorymanager.services;

import com.postmarkapp.postmark.Postmark;
import com.postmarkapp.postmark.client.ApiClient;
import com.postmarkapp.postmark.client.data.model.message.Message;
import com.postmarkapp.postmark.client.data.model.message.MessageResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscwii.repositorymanager.config.repoman.RepoManConfig;
import org.oscwii.repositorymanager.model.security.PasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void sendPasswordReset(String recipient, PasswordToken token)
    {
        String subject = "Repository Manager Password Reset";
        String content = MAIL_CONTENT.formatted(config.getBaseUrl(), token.getToken());
        Message mail = new Message("\"Repository Manager\" " + config.mailConfig.senderAddress(),
                recipient, subject, content);
        mail.setMessageStream("outbound");
        send(mail);
    }

    private void send(Message mail)
    {
        try
        {
            ApiClient client = Postmark.getApiClient(config.mailConfig.postmarkApiKey());
            MessageResponse res = client.deliverMessage(mail);

            if(res.getErrorCode() != 0)
                logger.error("Failed to send email: {}", res.getMessage());
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
