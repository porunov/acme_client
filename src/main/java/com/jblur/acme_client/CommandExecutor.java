package com.jblur.acme_client;

import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.command.authorization.AuthorizeDomainsCommand;
import com.jblur.acme_client.command.authorization.DeactivateDomainsAuthorizationCommand;
import com.jblur.acme_client.command.authorization.DownloadChallengesCommand;
import com.jblur.acme_client.command.authorization.VerifyDomainsCommand;
import com.jblur.acme_client.command.certificate.DownloadCertificatesCommand;
import com.jblur.acme_client.command.certificate.GenerateCertificateCommand;
import com.jblur.acme_client.command.certificate.RenewCertificateCommand;
import com.jblur.acme_client.command.certificate.RevokeCertificateCommand;
import com.jblur.acme_client.command.registration.*;
import com.jblur.acme_client.manager.RegistrationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CommandExecutor.class);

    private Parameters parameters;

    private String result;

    public CommandExecutor(Parameters parameters) {
        this.parameters = parameters;
    }

    private RegistrationManager getRegistrationManager() {
        RegistrationManager registrationManager = null;
        try {
            RegistrationCommand registrationCommand = new RegistrationCommand(parameters);
            result = executeACMECommand(registrationCommand);
            registrationManager = registrationCommand.getRegistrationManager();
        } catch (AccountKeyNotFoundException e) {
            LOG.error("Key not found exception", e);
        }
        return registrationManager;
    }

    private String executeACMECommand(ACMECommand acmeCommand) {
        acmeCommand.execute();
        return acmeCommand.getResult();
    }

    private void automaticallyUpdateAgreement(RegistrationManager registrationManager) {
        LOG.info("Trying yo update agreement");
        try {
            if (registrationManager == null) {
                registrationManager = getRegistrationManager();
            }
            if (registrationManager != null) {
                new UpdateAgreementCommand(parameters, registrationManager).execute();
            } else {
                LOG.warn("Can not create Registration. Can not update agreement");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.warn("Can not update agreement.", e);
        }
    }

    public void execute() {

        RegistrationManager registrationManager = null;

        LOG.info("Start execute a command '" + parameters.getCommand() + "'");

        switch (parameters.getCommand()) {
            case Parameters.COMMAND_REGISTER:
            case Parameters.COMMAND_GET_AGREEMENT_URL:
            case Parameters.COMMAND_UPDATE_AGREEMENT:
            case Parameters.COMMAND_ADD_EMAIL:
            case Parameters.COMMAND_DEACTIVATE_ACCOUNT:
            case Parameters.COMMAND_AUTHORIZE_DOMAINS:
            case Parameters.COMMAND_GENERATE_CERTIFICATE:
            case Parameters.COMMAND_RENEW_CERTIFICATE:
                registrationManager = getRegistrationManager();
                if (registrationManager == null) {
                    LOG.error("Can not get account information");
                    System.out.println(result);
                    return;
                }
        }

        if (parameters.isWithAgreementUpdate()) {
            automaticallyUpdateAgreement(registrationManager);
        }

        try {
            switch (parameters.getCommand()) {
                case Parameters.COMMAND_REGISTER:
                    break;
                case Parameters.COMMAND_GET_AGREEMENT_URL:
                    result = executeACMECommand(new GetAgreementURLCommand(parameters, registrationManager));
                    break;
                case Parameters.COMMAND_UPDATE_AGREEMENT:
                    result = executeACMECommand(new UpdateAgreementCommand(parameters, registrationManager));
                    break;
                case Parameters.COMMAND_ADD_EMAIL:
                    result = executeACMECommand(new AddEmailCommand(parameters, registrationManager));
                    break;
                case Parameters.COMMAND_DEACTIVATE_ACCOUNT:
                    result = executeACMECommand(new DeactivateAccountCommand(parameters, registrationManager));
                    break;
                case Parameters.COMMAND_AUTHORIZE_DOMAINS:
                    result = executeACMECommand(new AuthorizeDomainsCommand(parameters, registrationManager));
                    break;
                case Parameters.COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION:
                    result = executeACMECommand(new DeactivateDomainsAuthorizationCommand(parameters));
                    break;
                case Parameters.COMMAND_DOWNLOAD_CHALLENGES:
                    result = executeACMECommand(new DownloadChallengesCommand(parameters));
                    break;
                case Parameters.COMMAND_VERIFY_DOMAINS:
                    result = executeACMECommand(new VerifyDomainsCommand(parameters));
                    break;
                case Parameters.COMMAND_GENERATE_CERTIFICATE:
                    result = executeACMECommand(new GenerateCertificateCommand(parameters, registrationManager));
                    break;
                case Parameters.COMMAND_DOWNLOAD_CERTIFICATES:
                    result = executeACMECommand(new DownloadCertificatesCommand(parameters));
                    break;
                case Parameters.COMMAND_REVOKE_CERTIFICATE:
                    result = executeACMECommand(new RevokeCertificateCommand(parameters));
                    break;
                case Parameters.COMMAND_RENEW_CERTIFICATE:
                    result = executeACMECommand(new RenewCertificateCommand(parameters, registrationManager));
                    break;
                default:
                    LOG.error("You must choose one of the commands you want to execute (--help)");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.error("Key not found exception", e);
        }

        System.out.println(result);

    }

}
