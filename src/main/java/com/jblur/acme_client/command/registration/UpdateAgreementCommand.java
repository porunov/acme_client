package com.jblur.acme_client.command.registration;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.RegistrationManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UpdateAgreementCommand extends ACMECommand {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateAgreementCommand.class);
    private RegistrationManager registrationManagement;

    public UpdateAgreementCommand(Parameters parameters, RegistrationManager registrationManagement)
            throws AccountKeyNotFoundException {
        super(parameters);
        this.registrationManagement = registrationManagement;
    }

    @Override
    public void commandExecution() {
        try {
            URI agreementURI = (getParameters().getAgreementUrl() == null) ?
                    this.registrationManagement.getRegistration().getAgreement() :
                    new URL(getParameters().getAgreementUrl()).toURI();
            registrationManagement.modifyAgreement(agreementURI);
        } catch (IOException e) {
            LOG.error("Can not get agreement URL.", e);
            error = true;
        } catch (URISyntaxException e) {
            LOG.error("You have provided incorrect agreement URL.", e);
            error = true;
        } catch (AcmeException e) {
            LOG.error("Can not modify agreement", e);
            error = true;
        } catch (NullPointerException e){
            LOG.error("Agreement haven't been updated because your provider haven't returned an agreement URL.", e);
            error = true;
        }
    }


}
