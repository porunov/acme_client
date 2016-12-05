package com.jblur.acme_client.command;

public class AccountKeyNotFoundException extends Exception {
    public AccountKeyNotFoundException() {

    }

    public AccountKeyNotFoundException(String msg) {
        super(msg);
    }
}
