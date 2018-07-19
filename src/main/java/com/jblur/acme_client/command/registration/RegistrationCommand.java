package com.jblur.acme_client.command.registration;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.command.AuthorizedCommand;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationCommand extends AuthorizedCommand {

    private static final Logger LOG = LoggerFactory.getLogger(RegistrationCommand.class);

    public RegistrationCommand(Parameters parameters) throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        if (getParameters().getEmail() != null){
            ACMECommand acmeCommand = new AddEmailCommand(getParameters(), getAccountManager());
            acmeCommand.commandExecution();
            error = error | acmeCommand.error;
        }
    }


}
