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
        MDC.put(Constants.INBOUND_URL, message.getHeaders().get(Constants.INBOUND_URL, String.class));
        MDC.put(Constants.INBOUND_USERNAME, message.getHeaders().get(Constants.INBOUND_USERNAME, String.class));
        MDC.put(Constants.OUTBOUND_URL, message.getHeaders().get(Constants.OUTBOUND_URL, String.class));
        MDC.put(Constants.OUTBOUND_USERNAME, message.getHeaders().get(Constants.OUTBOUND_USERNAME, String.class));

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
            mailMessage.addHeader(Constants.X_IMAPMOVE_CORRELATION_ID, message.getHeaders().get(Constants.CORRELATION_ID, String.class));
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
