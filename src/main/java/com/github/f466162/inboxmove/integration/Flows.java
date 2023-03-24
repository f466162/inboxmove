package com.github.f466162.inboxmove.integration;

import com.github.f466162.inboxmove.Configuration;
import com.github.f466162.inboxmove.Constants;
import com.github.f466162.inboxmove.imap.ImapAuthenticator;
import com.github.f466162.inboxmove.imap.ImapOutboundMessageHandler;
import org.slf4j.MDC;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.mail.dsl.Mail;

import java.util.Objects;
import java.util.UUID;

public class Flows {

    public static IntegrationFlow createInboundFlow(Configuration.Inbound config, Configuration.Outbound outboundConfig) {
        return IntegrationFlow
                .from(Mail.imapIdleAdapter(config.getUrl())
                        .javaMailAuthenticator(new ImapAuthenticator(config.getUsername(), config.getPassword()))
                        .simpleContent(true)
                        .javaMailProperties(p -> p.put("mail.debug", config.isDebug())))
                .enrichHeaders(h -> h.correlationId(UUID.randomUUID().toString()))
                .enrichHeaders(h -> h.header(Constants.INBOUND_URL, config.getUrl()))
                .enrichHeaders(h -> h.header(Constants.OUTBOUND_URL, outboundConfig.getUrl()))
                .enrichHeaders(h -> h.header(Constants.OUTBOUND_USERNAME, outboundConfig.getUsername()))
                .log(LoggingHandler.Level.INFO, message -> {
                    MDC.put(Constants.ID, Objects.requireNonNull(message.getHeaders().getId()).toString());
                    MDC.put(Constants.INBOUND_URL, message.getHeaders().get(Constants.INBOUND_URL, String.class));
                    MDC.put(Constants.OUTBOUND_URL, message.getHeaders().get(Constants.OUTBOUND_URL, String.class));
                    MDC.put(Constants.CORRELATION_ID, message.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID, String.class));
                    return "Receiving message";
                })
                .channel(getChannelName(Direction.OUT, config.getOutboundReference()))
                .get();
    }

    public static IntegrationFlow createOutboundFlow(Configuration.Outbound config) {
        return IntegrationFlow
                .from(MessageChannels.queue(getChannelName(Direction.OUT, config.getId()), 1))
                .handle(ImapOutboundMessageHandler.builder()
                        .url(config.getUrl())
                        .username(config.getUsername())
                        .password(config.getPassword())
                        .folderName(config.getFolderName())
                        .debug(String.valueOf(config.isDebug()))
                        .build()
                )
                .get();
    }

    public static String getFlowName(Direction direction, String reference) {
        return String.format("FLOW_%s:%s", direction.name(), reference);
    }

    public static String getChannelName(Direction direction, String reference) {
        return String.format("CHAN_%s:%s", direction.name(), reference);
    }

    public enum Direction {
        IN, OUT
    }
}
