package com.github.f466162.inboxmove.imap;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class ImapAuthenticator extends Authenticator {
    private final PasswordAuthentication authentication;

    public ImapAuthenticator(String username, String password) {
        super();
        this.authentication = new PasswordAuthentication(username, password);
    }

    public static ImapAuthenticator getInstance(String username, String password) {
        return new ImapAuthenticator(username, password);
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }
}
