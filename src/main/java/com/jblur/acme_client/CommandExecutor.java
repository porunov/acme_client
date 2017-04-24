package com.jblur.acme_client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

    public static final String RESULT_ERROR;

    static {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("status", gson.toJsonTree("error"));
        RESULT_ERROR = gson.toJson(jsonObject);
    }

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
            if (registrationManager == null) {
                LOG.error("Cannot get account information.");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.error("Account key not found.", e);
        }
        return registrationManager;
    }

    private String executeACMECommand(ACMECommand acmeCommand) {
        acmeCommand.execute();
        return acmeCommand.getResult();
    }

    private void automaticallyUpdateAgreement(RegistrationManager registrationManager) {
        LOG.info("Trying to update agreement");
        try {
            if (registrationManager == null) {
                registrationManager = getRegistrationManager();
            }
            if (registrationManager != null) {
                new UpdateAgreementCommand(parameters, registrationManager).execute();
            } else {
                LOG.warn("Cannot create registration. Cannot update agreement.");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.warn("Account key not found. Cannot update agreement.", e);
        } catch (Exception e){
            LOG.warn("Cannot update agreement.", e);
        }
    }

    public void execute() {

        RegistrationManager registrationManager = null;

        LOG.info("Start execute command '" + parameters.getCommand() + "'");

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
                    System.out.println(RESULT_ERROR);
                    LOG.error("Cannot get registration.");
                    return;
                }
        }

        if (parameters.isWithAgreementUpdate()) {
            //Strange. After the registration CA returns unworkable info. We need to get registration one more time.
            registrationManager = (parameters.getCommand().equals(Parameters.COMMAND_REGISTER))?
                    null:registrationManager;
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
                    LOG.error("No command specified. You must specify a command to execute, use --help for a list of available commands.");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.error("Account key not found.", e);
            result=RESULT_ERROR;
        }

        System.out.println(result);
    }

}
