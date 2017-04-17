package com.jblur.acme_client.command.authorization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.ChallengeManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class VerifyDomainsCommand extends AuthorizationCommand {
    private static final Logger LOG = LoggerFactory.getLogger(VerifyDomainsCommand.class);

    public VerifyDomainsCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        HashSet<String> verifiedDomains = new HashSet<>();
        HashSet<String> failedDomains = new HashSet<>();

        List<Authorization> authorizationList = getNotExpiredAuthorizations();
        if (authorizationList == null) {
            LOG.error("Cannot read file: " +
                    AUTHORIZATION_FILE_PATH);
            error = true;
            return;
        }

        for (Authorization authorization : authorizationList) {
            if (getParameters().getDomains() == null || getParameters().getDomains().contains(authorization.getDomain())) {
                try {
                    new ChallengeManager(authorization, getChallengeType()).validateChallenge(60000);
                    if (!verifiedDomains.contains(authorization.getDomain()))
                        verifiedDomains.add(authorization.getDomain());
                } catch (TimeoutException ex) {
                    LOG.warn("Authorization " + authorization.getLocation() + " haven't been verified. Time out exception", ex);
                    if (!failedDomains.contains(authorization.getDomain()))
                        failedDomains.add(authorization.getDomain());
                } catch (AcmeException ex) {
                    LOG.warn("Authorization " + authorization.getLocation() + " haven't been verified.", ex);
                    if (!failedDomains.contains(authorization.getDomain()))
                        failedDomains.add(authorization.getDomain());
                }
            }
        }

        error = error || !writeAuthorizationList(authorizationList);

        failedDomains.removeAll(verifiedDomains);

        if (failedDomains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains, new TypeToken<HashSet<String>>() {
            }.getType());
            result.add("failed_domains", failedDomainsJsonElement);
            error=true;
        }
    }
}
