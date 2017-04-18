package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Registration;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.util.Collection;

public class AuthorizationManager {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationManager.class);

    private Authorization authorization;

    public AuthorizationManager(Registration registration, String domainName) throws AcmeException {
        this.authorization = registration.authorizeDomain(domainName);
    }

    public AuthorizationManager(Session session, URI authUri) {
        this.authorization = Authorization.bind(session, authUri);
    }

    public AuthorizationManager(Authorization authorization) {
        this.authorization = authorization;
    }

    public Authorization getAuthorization() {
        return this.authorization;
    }

    public Collection<Challenge> getHttp01OrDns01Challenges() {
        return this.authorization.findCombination(Http01Challenge.TYPE, Dns01Challenge.TYPE);
    }

    /**
     * Your challenge should be accessible via next url:
     * http://${domain}/.well-known/acme-challenge/${token}
     * it must contain "content" of the challenge.
     * <p>
     * Content-Type of the header must be "text/plain" or absent
     * <p>
     * The challenge is completed when the CA was able to download that file and found content in it.
     *
     * @return HTTP01 Challenge
     */
    public Http01Challenge getHttp01Challenge() {
        return this.authorization.findChallenge(Http01Challenge.TYPE);
    }

    /**
     * Your domain name: _acme-challenge.${domain} should has
     * a TXT record with the "digest" string as a value
     * <p>
     * The challenge is completed when the CA was able to access that domain with the correct digest
     *
     * @return DNS01 Challenge
     */
    public Dns01Challenge getDns01Challenge() {
        return this.authorization.findChallenge(Dns01Challenge.TYPE);
    }

    public void deactivate() throws AcmeException {
        this.authorization.deactivate();
    }

}
