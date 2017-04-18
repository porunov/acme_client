package com.jblur.acme_client.command.registration;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.RegistrationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;

public class GetAgreementURLCommand extends ACMECommand {
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationCommand.class);
    private RegistrationManager registrationManagement;
    private URI agreementUri = null;

    public GetAgreementURLCommand(Parameters parameters, RegistrationManager registrationManagement)
            throws AccountKeyNotFoundException {
        super(parameters);
        this.registrationManagement = registrationManagement;
    }

    @Override
    public void commandExecution() {
        try {
            agreementUri = registrationManagement.getRegistration().getAgreement();
            JsonElement agreementUrlJsonElement = getGson().toJsonTree(agreementUri.toString(), new TypeToken<String>() {
            }.getType());
            result.add("agreement_url", agreementUrlJsonElement);
        } catch (Exception e) {
            LOG.error("Cannot get an agreement URL", e);
            error = true;
        }
    }

    public URI getAgreementUri() {
        return this.agreementUri;
    }
}
