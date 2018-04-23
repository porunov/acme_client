package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.concurrent.TimeoutException;

public class ChallengeManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeManager.class);

    private Challenge challenge;

    public ChallengeManager(Challenge challenge) {
        this.challenge = challenge;
    }

    public ChallengeManager(Challenge challenge, Session session) throws AcmeException {
        this.challenge = challenge;
        try {
            challenge.rebind(session);
        } catch (Exception ex) {
            LOG.warn("Cannot rebind challenge: " + challenge.getLocation() + " to session: " +
                    session.getServerUri().toString(), ex);
        }
    }

    public ChallengeManager(Authorization authorization, String type) throws AcmeException {
        this.challenge = authorization.findChallenge(type);
        if (this.challenge == null) throw new AcmeException();
    }

    public ChallengeManager(Authorization authorization, String type, Session session) throws AcmeException {
        this.challenge = authorization.findChallenge(type);
        if (this.challenge == null) throw new AcmeException();
        try {
            challenge.rebind(session);
        } catch (Exception ex) {
            LOG.warn("Can not rebind challenge: " + challenge.getLocation() + " to session: " +
                    session.getServerUri().toString(), ex);
        }
    }

    public ChallengeManager(Session session, URL challengeURL) throws AcmeException {
        this.challenge = Challenge.bind(session, challengeURL);
    }

    public Challenge getChallenge() {
        return this.challenge;
    }

    public void validateChallenge(int maxTimeToWait) throws AcmeException, TimeoutException {
        challenge.trigger();
        long sleepTime = 3000L;
        while (challenge.getStatus() != Status.VALID) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                //TODO: Log
            }
            challenge.update();
            maxTimeToWait -= (int) sleepTime;
            if (maxTimeToWait < 0) {
                throw new TimeoutException();
            }
            if (challenge.getStatus() == Status.INVALID) {
                throw new AcmeException("challenge invalid");
            }
        }
    }
}
