package com.jblur.acme_client.command.registration;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.RegistrationManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddEmailCommand extends ACMECommand {
    private static final Logger LOG = LoggerFactory.getLogger(AddEmailCommand.class);
    private RegistrationManager registrationManagement;

    public AddEmailCommand(Parameters parameters, RegistrationManager registrationManagement)
            throws AccountKeyNotFoundException {
        super(parameters);
        this.registrationManagement = registrationManagement;
    }

    @Override
    public void commandExecution() {
        try {
            registrationManagement.addContact(getParameters().getEmail());
        } catch (AcmeException e) {
            LOG.error("Cannot add email", e);
            error = true;
        }
    }

}
