package com.github.f466162.inboxmove.imap;

import com.github.f466162.inboxmove.Configuration;
import com.github.f466162.inboxmove.Constants;
import com.sun.mail.imap.IMAPStore;
import jakarta.mail.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;

import java.util.Objects;
import java.util.Properties;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImapOutboundMessageHandler extends AbstractMessageHandler {

    private Configuration.Outbound config;

    @Override
    public String getComponentType() {
        return "mail:outbound-channel-adapter";
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        URLName urlName = new URLName(config.getUrl());
        Properties properties = Configuration.getJakartaMailProperties(config);
        ImapAuthenticator authenticator = ImapAuthenticator
                .getInstance(config.getUsername(), config.getPassword());
        Session session = Session.getInstance(properties, authenticator);

        MDC.put(Constants.ID, Objects.requireNonNull(message.getHeaders().getId()).toString());
        MDC.put(Constants.CORRELATION_ID, String.valueOf(message.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID)));
        MDC.put(Constants.OUTBOUND_URL, config.getUrl());
        MDC.put(Constants.OUTBOUND_FOLDER, config.getFolderName());

        try (IMAPStore store = (IMAPStore) session.getStore(urlName)) {
            if (!store.isConnected()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Connecting to store");
                }

                store.connect();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Opening folder");
            }

            Folder folder = store.getFolder(config.getFolderName());

            if (!folder.exists()) {
                logger.info("Creating Folder on IMAP account");
                folder.create(Folder.HOLDS_MESSAGES);
            }

            jakarta.mail.Message mailMessage = (jakarta.mail.Message) message.getPayload();
            mailMessage.setFlag(Flags.Flag.SEEN, false);
            String messageId = mailMessage.getHeader(Constants.MAIL_MESSAGE_ID)[0];

            MDC.put(Constants.MESSAGE_ID, messageId);
            logger.info("Appending message");

            folder.appendMessages(new jakarta.mail.Message[]{mailMessage});
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        MDC.clear();
    }
}
