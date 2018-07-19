package com.jblur.acme_client.command;

import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.manager.AccountManager;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyPair;

/**
 * Created by Oleksandr Porunov on 7/20/18.
 */
public abstract class AuthorizedCommand extends ACMECommand {
    private static final Logger LOG = LoggerFactory.getLogger(AuthorizedCommand.class);

    private AccountManager accountManager;

    public AuthorizedCommand(Parameters parameters) throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
        accountManager = new AccountManager(getAccountKey(), getSession(), parameters.isWithAgreementUpdate());
    }

    private KeyPair getAccountKey() throws AccountKeyNotFoundException{
        try {
            return IOManager.readKeyPairFromPrivateKey(getParameters().getAccountKey());
        } catch (IOException e) {
            LOG.error("Cannot read account key. Make sure that it is in a proper format.", e);
            throw new AccountKeyNotFoundException(e);
        }
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }
}
