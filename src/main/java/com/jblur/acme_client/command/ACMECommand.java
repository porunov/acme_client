package com.jblur.acme_client.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import org.shredzone.acme4j.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyPair;

public abstract class ACMECommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ACMECommand.class);
    public boolean error = false;
    public JsonObject result = new JsonObject();
    private Parameters parameters;
    private Session session;
    private Gson gson = new Gson();

    public ACMECommand(Parameters parameters) throws AccountKeyNotFoundException {
        this.parameters = parameters;

        KeyPair accountKey = getAccountKey();
        if (accountKey == null) throw new AccountKeyNotFoundException();

        this.session = new Session(this.parameters.getAcmeServerUrl(), accountKey);
    }

    private KeyPair getAccountKey() {
        KeyPair accountKey = null;
        try {
            accountKey = IOManager.readKeyPairFromPrivateKey(parameters.getAccountKey());
        } catch (IOException e) {
            LOG.error("Can not read account key. Make sure that it is in a proper format.", e);
        }
        return accountKey;
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    public Session getSession() {
        return this.session;
    }

    public Gson getGson() {
        return this.gson;
    }

    public String getResult() {
        return gson.toJson(result);
    }

    private void preExecution() {
        error = false;
        result = new JsonObject();
    }

    @Override
    public void execute() {
        preExecution();
        commandExecution();
        postExecution();
    }

    private void postExecution() {
        result.add("status", getGson().toJsonTree((error) ? "error" : "ok"));
    }

    public abstract void commandExecution();

}
