package com.jblur.acme_client.command.registration;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.RegistrationManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeactivateAccountCommand extends ACMECommand {

    private static final Logger LOG = LoggerFactory.getLogger(DeactivateAccountCommand.class);
    private RegistrationManager registrationManagement;

    public DeactivateAccountCommand(Parameters parameters, RegistrationManager registrationManagement)
            throws AccountKeyNotFoundException {
        super(parameters);
        this.registrationManagement = registrationManagement;
    }


    @Override
    public void commandExecution() {
        try {
            registrationManagement.deactivateAccount();
        } catch (AcmeException e) {
            LOG.error("Can not deactivate account", e);
            error = true;
        }
    }

}
