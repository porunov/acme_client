package com.jblur.acme_client.command.authorization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            LOG.error("Cannot read file: " +
                    AUTHORIZATION_FILE_PATH);
            error = true;
            return;
        }

        List<String> failedAuthorizations = new LinkedList<>();

        List<Authorization> authorizationList = new LinkedList<>();

        for (Authorization authorization : allAuthrizationsList) {
            if (getParameters().getDomains() == null || getParameters().getDomains().contains(authorization.getDomain())) {
                try {
                    authorization.deactivate();
                } catch (AcmeException e) {
                    LOG.error("Cannot deactivate authorization: "+authorization.getLocation().toString(), e);
                    failedAuthorizations.add(authorization.getLocation().toString());
                    error = true;
                }
            } else {
                authorizationList.add(authorization);
            }
        }

        error = error || !writeAuthorizationList(authorizationList);

        if (failedAuthorizations.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedAuthorizations, new TypeToken<List<String>>() {
            }.getType());
            result.add("failed_authorizations", failedDomainsJsonElement);
            error=true;
        }
    }

}
