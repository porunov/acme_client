package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.toolbox.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChallengeManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeManager.class);

    private Challenge challenge;

    public ChallengeManager(Challenge challenge) {
        this.challenge = challenge;
    }

    public ChallengeManager(Challenge challenge, Login login) throws AcmeException {
        this.challenge = challenge;
        try {
            challenge.rebind(login);
        } catch (Exception ex) {
            LOG.warn("Cannot rebind challenge: " + challenge.getLocation() + " to login: " +
                    login.getAccountLocation().toString(), ex);
        }
    }

    public ChallengeManager(Authorization authorization, String type) throws AcmeException {
        this.challenge = authorization.findChallenge(type);
        if (this.challenge == null) throw new AcmeException("CA didn't provide any challenge for type: "+type);
    }

    public ChallengeManager(Authorization authorization, String type, Login login) throws AcmeException {
        this.challenge = authorization.findChallenge(type);
        if (this.challenge == null) throw new AcmeException();
        try {
            challenge.rebind(login);
        } catch (Exception ex) {
            LOG.warn("Can not rebind challenge: " + challenge.getLocation() + " to login: " +
                    login.getAccountLocation().toString(), ex);
        }
    }

    public ChallengeManager(Login login, JSON data) throws AcmeException {
        this.challenge = login.createChallenge(data);
    }

    public Challenge getChallenge() {
        return this.challenge;
    }

    public boolean validateChallenge() throws AcmeException {
        return ValidationService.validate(new ResourceWithStatusWrapper() {
            @Override
            public Status getStatus() {
                return challenge.getStatus();
            }

            @Override
            public void trigger() throws AcmeException {
                challenge.trigger();
            }

            @Override
            public void update() throws AcmeException {
                challenge.update();
            }

            @Override
            public String getLocation() {
                return challenge.getLocation().toString();
            }

            @Override
            public void failIfInvalid() throws AcmeException {
                if (challenge.getStatus() == Status.INVALID) {
                    throw new AcmeException("Challenge invalid: "+getLocation());
                }
            }
        });
    }
}
