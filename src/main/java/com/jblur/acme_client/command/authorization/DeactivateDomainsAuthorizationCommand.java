package com.jblur.acme_client.command.authorization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class DeactivateDomainsAuthorizationCommand extends AuthorizationCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DeactivateDomainsAuthorizationCommand.class);

    public DeactivateDomainsAuthorizationCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    @Override
    public void commandExecution() {

        List<Authorization> allAuthrizationsList = getNotExpiredAuthorizations();
        if (allAuthrizationsList == null) {
            LOG.error("Can not read file: " +
                    Paths.get(getParameters().getWorkDir(), Parameters.AUTHORIZATION_URI_LIST).toString());
            error = true;
            return;
        }

        List<String> failedDomains = new LinkedList<>();

        List<Authorization> authorizationList = new LinkedList<>();

        for (Authorization authorization : allAuthrizationsList) {
            if (getParameters().getDomains() == null || getParameters().getDomains().contains(authorization.getDomain())) {
                try {
                    authorization.deactivate();
                } catch (AcmeException e) {
                    LOG.error("Can not deactivate authorization", e);
                    failedDomains.add(authorization.getDomain());
                    error = true;
                }
            } else {
                authorizationList.add(authorization);
            }
        }

        error = error || !writeAuthorizationList(authorizationList);

        if (failedDomains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains, new TypeToken<List<String>>() {
            }.getType());
            result.add("failed_domains", failedDomainsJsonElement);
        }
    }

}
