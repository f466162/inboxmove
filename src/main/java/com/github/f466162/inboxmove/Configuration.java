package com.github.f466162.inboxmove;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.integration.support.PropertiesBuilder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "inboxmove", ignoreUnknownFields = false)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Validated
public class Configuration {
    @Size(min = 1)
    private List<Inbound> inbound = new ArrayList<>();
    @Size(min = 1)
    private List<Outbound> outbound = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Inbound extends Connection {
        @NotBlank
        private String outboundReference;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Outbound extends Connection {
        @NotBlank
        private String folderName = "INBOX";
    }

    public static Properties getJakartaMailProperties(Connection config) {
        return new PropertiesBuilder()
                .put("mail.debug", String.valueOf(config.isDebug()))
                .put("mail.imap.starttls.enable", config.isStartTLS())
                .put("mail.imap.starttls.require", config.isRequireTLS())
                .put("mail.imap.minidletime", config.getMinIdleTime())
                .put("mail.imap.appendbuffersize", config.getAppendBufferSize())
                .put("mail.imap.connectiontimeout", config.getConnectionTimeout())
                .put("mail.imap.timeout", config.getTimeout())
                .put("mail.imap.writetimeout", config.getWriteTimeout()).get();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Connection {
        @NotBlank
        private String id;
        @NotBlank
        @Pattern(regexp = "^imap(s)?://.+")
        private String url;
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotNull
        private boolean debug = false;
        @NotNull
        private boolean startTLS = true;
        @NotNull
        private boolean requireTLS = true;
        @Min(1)
        private int minIdleTime = 10;
        @Min(1)
        private int connectionTimeout = 60_000;
        @Min(1)
        private int timeout = 120_000;
        @Min(1)
        private int writeTimeout = 120_000;
        @Min(1)
        private int appendBufferSize = 1024 * 1024;
    }
}

