package com.github.f466162.inboxmove.imap;

import com.github.f466162.inboxmove.Constants;
import com.sun.mail.imap.IMAPStore;
import jakarta.mail.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.support.PropertiesBuilder;
import org.springframework.messaging.Message;

import java.util.Objects;
import java.util.Properties;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImapOutboundMessageHandler extends AbstractMessageHandler {
    private String url;
    private String username;
    private String password;
    private String folderName;
    private String debug;

    @Override
    public String getComponentType() {
        return "mail:outbound-channel-adapter";
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        URLName urlName = new URLName(url);
        Properties properties = new PropertiesBuilder()
                .put("mail.imap.starttls.enable", "true")
                .put("mail.imap.starttls.require", "true")
                .put("mail.debug", debug)
                .get();
        ImapAuthenticator authenticator = ImapAuthenticator
                .getInstance(username, password);
        Session session = Session.getInstance(properties, authenticator);

        MDC.put(Constants.ID, Objects.requireNonNull(message.getHeaders().getId()).toString());
        MDC.put(Constants.CORRELATION_ID, String.valueOf(message.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID)));
        MDC.put(Constants.OUTBOUND_URL, url);
        MDC.put(Constants.OUTBOUND_FOLDER, folderName);

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

            Folder folder = store.getFolder(folderName);

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
