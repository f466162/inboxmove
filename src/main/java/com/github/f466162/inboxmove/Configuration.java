package com.github.f466162.inboxmove;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

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
    }
}

