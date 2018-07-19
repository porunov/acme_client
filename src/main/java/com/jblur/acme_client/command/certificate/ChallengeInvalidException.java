package com.jblur.acme_client.command.certificate;

public class ChallengeInvalidException extends Exception {
    public ChallengeInvalidException() {

    }

    public ChallengeInvalidException(String msg) {
        super(msg);
    }
}
