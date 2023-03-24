package com.github.f466162.inboxmove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class InboxMoveApplication {

    public static void main(String[] args) {
        SpringApplication.run(InboxMoveApplication.class, args);
    }

}
