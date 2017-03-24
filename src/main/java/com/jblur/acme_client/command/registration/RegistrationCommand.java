package com.jblur.acme_client.command.registration;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.RegistrationManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationCommand extends ACMECommand {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationCommand.class);

    private RegistrationManager registrationManagement = null;

    public RegistrationCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        try {
            if (getParameters().getEmail() == null)
                this.registrationManagement = new RegistrationManager(getSession());
            else
                this.registrationManagement = new RegistrationManager(getSession(), getParameters().getEmail());
        } catch (AcmeException e) {
            LOG.error("Problem with registration/authorization", e);
            error = true;
        } catch (Exception e){
            LOG.error("Registration/authorization failed without getting an error for your provider." +
                    "Please check if acme provider server url and your account private key are correct.");
            error = true;
        }
    }

    public RegistrationManager getRegistrationManager() {
        return this.registrationManagement;
    }

}
