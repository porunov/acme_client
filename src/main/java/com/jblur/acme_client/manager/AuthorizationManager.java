package com.jblur.acme_client.manager;

import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.Collection;

public class AuthorizationManager {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationManager.class);

    private Authorization authorization;

    public AuthorizationManager(Account account, String domain) throws AcmeException {
        this.authorization = account.preAuthorizeDomain(domain);
    }

    public AuthorizationManager(Login login, URL authUrl) {
        this.authorization = login.bindAuthorization(authUrl);
    }

    public AuthorizationManager(Authorization authorization) {
        this.authorization = authorization;
    }

    public Authorization getAuthorization() {
        return this.authorization;
    }

    public Collection<Challenge> getChallenges(){
        return authorization.getChallenges();
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

    public TlsAlpn01Challenge getTlsAlpn01Challenge(){
        return this.authorization.findChallenge(TlsAlpn01Challenge.TYPE);
    }

    public boolean authorizeDomain() throws AcmeException{
        return ValidationService.validate(new ResourceWithStatusWrapper() {
            @Override
            public Status getStatus() {
                return authorization.getStatus();
            }

            @Override
            public void trigger() throws AcmeException {
            }

            @Override
            public void update() throws AcmeException {
                authorization.update();
            }

            @Override
            public String getLocation() {
                return authorization.getLocation().toString();
            }

            @Override
            public void failIfInvalid() throws AcmeException {
                if(isAuthorizationUnusable()){
                    throw new AcmeException("Authorization: "+authorization.getLocation().toString()+" cannot be used " +
                            "anymore");
                }
            }

            private boolean isAuthorizationUnusable(){
                return authorization.getStatus() == Status.INVALID ||
                        authorization.getStatus() == Status.EXPIRED ||
                        authorization.getStatus() == Status.DEACTIVATED ||
                        authorization.getStatus() == Status.REVOKED;
            }
        });
    }

}
