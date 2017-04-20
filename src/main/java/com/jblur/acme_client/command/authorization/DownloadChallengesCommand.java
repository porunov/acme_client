package com.jblur.acme_client.command.authorization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import org.shredzone.acme4j.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.List;

public class DownloadChallengesCommand extends AuthorizationCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadChallengesCommand.class);

    public DownloadChallengesCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        List<Authorization> authorizationList = getNotExpiredAuthorizations();
        if (authorizationList == null) {
            LOG.error("Cannot read file: " +
                    AUTHORIZATION_FILE_PATH);
            error = true;
            return;
        }

        HashSet<String> domains = getDomains(authorizationList);
        HashSet<String> authorizedDomains = new HashSet<>();

        for (Authorization authorization : authorizationList) {
            authorizedDomains.add(authorization.getDomain());
            if (domains.contains(authorization.getDomain())) {
                try {
                    writeChallengeByAuthorization(new AuthorizationManager(authorization));
                    domains.remove(authorization.getDomain());
                } catch (Exception e) {
                    LOG.warn("Cannot get challenge for authorization: " + authorization.getLocation()
                            + "\nDomain: " + authorization.getDomain(), e);
                }
            }
        }

        for(String domain : domains){
            if(!authorizedDomains.contains(domain)){
                LOG.error("Domain " + domain + " is not authorized. Please, authorize it first.");
            }else {
                LOG.error("Domain " + domain + " is not verified. Please, check warnings.");
            }
        }

        error = error || !writeAuthorizationList(authorizationList);

        if (domains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(domains,
                    new TypeToken<HashSet<String>>() {}.getType());
            result.add("failed_domains", failedDomainsJsonElement);
            error=true;
        }
    }
}
