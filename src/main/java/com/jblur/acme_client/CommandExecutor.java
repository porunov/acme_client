package com.jblur.acme_client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.command.certificate.*;
import com.jblur.acme_client.command.registration.*;
import com.jblur.acme_client.manager.AccountManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeUserActionRequiredException;
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

    public void execute() {

        AccountManager accountManager = null;

        LOG.info("Start execute command '" + parameters.getCommand() + "'");

        switch (parameters.getCommand()) {
            case Parameters.COMMAND_REGISTER:
            case Parameters.COMMAND_ADD_EMAIL:
            case Parameters.COMMAND_DEACTIVATE_ACCOUNT:
                accountManager = getAccountManager();
                if (accountManager == null) {
                    System.out.println(RESULT_ERROR);
                    LOG.error("Cannot get registration.");
                    return;
                }
        }

        try {
            switch (parameters.getCommand()) {
                case Parameters.COMMAND_REGISTER: case Parameters.COMMAND_ADD_EMAIL:
                    break;
                case Parameters.COMMAND_GET_AGREEMENT_URL:
                    result = executeACMECommand(new GetAgreementURLCommand(parameters));
                    break;
                case Parameters.COMMAND_DEACTIVATE_ACCOUNT:
                    result = executeACMECommand(new DeactivateAccountCommand(parameters, accountManager));
                    break;
                case Parameters.COMMAND_ORDER_CERTIFICATE:
                    result = executeACMECommand(new OrderCertificateCommand(parameters));
                    break;
                case Parameters.COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION:
                    result = executeACMECommand(new DeactivateDomainsOrderCommand(parameters));
                    break;
                case Parameters.COMMAND_DOWNLOAD_CHALLENGES:
                    result = executeACMECommand(new DownloadChallengesCommand(parameters));
                    break;
                case Parameters.COMMAND_VERIFY_DOMAINS:
                    result = executeACMECommand(new VerifyDomainsCommand(parameters));
                    break;
                case Parameters.COMMAND_GENERATE_CERTIFICATE:
                    result = executeACMECommand(new GenerateCertificateCommand(parameters));
                    break;
                case Parameters.COMMAND_DOWNLOAD_CERTIFICATES:
                    result = executeACMECommand(new DownloadCertificatesCommand(parameters));
                    break;
                case Parameters.COMMAND_REVOKE_CERTIFICATE:
                    result = executeACMECommand(new RevokeCertificateCommand(parameters));
                    break;
                default:
                    LOG.error("No command specified. You must specify a command to execute, use --help for a list of available commands.");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.error("Account key not found.", e);
            result=RESULT_ERROR;
        } catch (AcmeUserActionRequiredException e){
            LOG.error("Manual action required. Please visit url with instructions: "+e.getInstance().toString(), e);
            if(e.getTermsOfServiceUri()!=null){
                LOG.error("You should agree to terms of service: "+e.getTermsOfServiceUri());
            }
            result=RESULT_ERROR;
        } catch (AcmeException e) {
            e.printStackTrace();
            result=RESULT_ERROR;
        }

        System.out.println(result);
    }

    private AccountManager getAccountManager() {
        AccountManager accountManager = null;
        try {
            RegistrationCommand registrationCommand = new RegistrationCommand(parameters);
            result = executeACMECommand(registrationCommand);
            accountManager = registrationCommand.getAccountManager();
            if (accountManager == null) {
                LOG.error("Cannot get account information.");
            }
        } catch (AccountKeyNotFoundException e) {
            LOG.error("Account key not found.", e);
        } catch (AcmeException e) {
            LOG.error("Cannot be authorized.", e);
        }
        return accountManager;
    }

    private String executeACMECommand(ACMECommand acmeCommand) {
        acmeCommand.execute();
        return acmeCommand.getResult();
    }

}
