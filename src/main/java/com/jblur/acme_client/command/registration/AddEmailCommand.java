package com.jblur.acme_client.command.registration;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.manager.AccountManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class AddEmailCommand extends ACMECommand {
    private static final Logger LOG = LoggerFactory.getLogger(AddEmailCommand.class);

    private static final String MAILTO_SCHEME = "mailto:";

    private AccountManager registrationManagement;

    public AddEmailCommand(Parameters parameters, AccountManager registrationManagement){
        super(parameters);
        this.registrationManagement = registrationManagement;
    }

    @Override
    public void commandExecution() {
        try {
            boolean emailExists = false;
            URI emailURI = new URI(MAILTO_SCHEME+getParameters().getEmail());

            for(URI contact : registrationManagement.getAccount().getContacts()){
                if (emailURI.equals(contact)){
                    emailExists = true;
                    break;
                }
            }

            if(!emailExists){
                registrationManagement.addContact(emailURI);
            }

        } catch (AcmeException | URISyntaxException e) {
            LOG.error("Cannot add email : "+getParameters().getEmail(), e);
            error = true;
        }
    }

}
