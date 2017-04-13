package com.jblur.acme_client.command.authorization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import org.shredzone.acme4j.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DownloadChallengesCommand extends AuthorizationCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadChallengesCommand.class);

    public DownloadChallengesCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        HashSet<String> succeedDomains = new HashSet<>();
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
                    writeChallengeByAuthorization(new AuthorizationManager(authorization));
                    if (!succeedDomains.contains(authorization.getDomain()))
                        succeedDomains.add(authorization.getDomain());
                } catch (Exception e) {
                    LOG.error("Cannot get challenges for authorization: " + authorization.getLocation()
                            + "\nDomain: " + authorization.getDomain(), e);
                    if (!failedDomains.contains(authorization.getDomain()))
                        failedDomains.add(authorization.getDomain());
                }
            }
        }

        error = error || !writeAuthorizationList(authorizationList);

        for(String failedDomain:failedDomains)
            if(succeedDomains.contains(failedDomain))
                failedDomains.remove(failedDomain);
        
        if (failedDomains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains,
                    new TypeToken<HashSet<String>>() {}.getType());
            result.add("failed_domains_to_download_authorizations", failedDomainsJsonElement);
            error=true;
        }
    }
}
