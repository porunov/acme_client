package com.jblur.acme_client.command.authorization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import com.jblur.acme_client.manager.RegistrationManager;
import org.shredzone.acme4j.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class AuthorizeDomainsCommand extends AuthorizationCommand {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizeDomainsCommand.class);
    private RegistrationManager registrationManagement;

    public AuthorizeDomainsCommand(Parameters parameters, RegistrationManager registrationManagement)
            throws AccountKeyNotFoundException {
        super(parameters);
        this.registrationManagement = registrationManagement;
    }

    @Override
    public void commandExecution() {
        List<String> failedDomains = new LinkedList<>();
        List<Authorization> authorizationList = new LinkedList<>();

        for (String domain : getParameters().getDomains()) {
            try {
                AuthorizationManager authorizationManagement = new AuthorizationManager(
                        registrationManagement.getRegistration(), domain);
                authorizationList.add(authorizationManagement.getAuthorization());
                writeChallengeByAuthorization(authorizationManagement);
            } catch (Exception ex) {
                LOG.error("Can not authorize domain: " + domain + "\n" +
                        "It will be skipped", ex);
                error = true;
                failedDomains.add(domain);
            }
        }

        List<Authorization> savedAuthorizations = getNotExpiredAuthorizations();
        if (savedAuthorizations != null) {
            authorizationList.addAll(savedAuthorizations);
        }

        error = error || !writeAuthorizationList(authorizationList);

        if (failedDomains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains, new TypeToken<List<String>>() {
            }.getType());
            result.add("failed_domains", failedDomainsJsonElement);
        }
    }
}
