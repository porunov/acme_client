package com.jblur.acme_client.command.authorization;

import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public abstract class AuthorizationCommand extends ACMECommand {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationCommand.class);

    public AuthorizationCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    public List<Authorization> getNotExpiredAuthorizations() {
        List<Authorization> authorizationList = null;
        if (IOManager.isFileExists(Paths.get(getParameters().getWorkDir(), Parameters.AUTHORIZATION_URI_LIST).toString())) {
            try {
                List<Authorization> oldAuthorizations = (List<Authorization>) IOManager.deserialize(
                        Paths.get(getParameters().getWorkDir(), Parameters.AUTHORIZATION_URI_LIST).toString());
                authorizationList = new LinkedList<>();
                for (Authorization authorization : oldAuthorizations) {
                    if (authorization.getExpires().getTime() > System.currentTimeMillis()) {
                        authorization.rebind(getSession());
                        authorizationList.add(authorization);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                LOG.warn("Your file can not be read. It has a bad structure", e);
            }
        }
        return authorizationList;
    }


    public boolean writeAuthorizationList(List<Authorization> authorizationList) {
        try {
            IOManager.serialize(authorizationList,
                    Paths.get(getParameters().getWorkDir(), Parameters.AUTHORIZATION_URI_LIST).toString());
        } catch (IOException e) {
            LOG.error("Can not write authorization list to file: " + Paths.get(getParameters().getWorkDir(),
                    Parameters.AUTHORIZATION_URI_LIST).toString() + "\n Please check permissions of the file.", e);
            return false;
        }
        return true;
    }

    public void writeChallengeByAuthorization(AuthorizationManager authorizationManagement) throws Exception {
        switch (getChallengeType()) {
            case Http01Challenge.TYPE:
                Http01Challenge http01Challenge = authorizationManagement.getHttp01Challenge();
                String path;
                if (getParameters().isOneDirForWellKnown()) {
                    path = Paths.get(getParameters().getWellKnownDir(), http01Challenge.getToken()).toString();
                } else {
                    path = Paths.get(getParameters().getWellKnownDir(),
                            authorizationManagement.getAuthorization().getDomain()).toString();
                    IOManager.createDirectories(path);
                    path = Paths.get(path, http01Challenge.getToken()).toString();
                }
                IOManager.writeString(path, http01Challenge.getAuthorization());
                break;
            case Dns01Challenge.TYPE:
                Dns01Challenge dns01Challenge = authorizationManagement.getDns01Challenge();
                IOManager.writeString(
                        Paths.get(getParameters().getDnsDigestDir(),
                                authorizationManagement.getAuthorization().getDomain() + "_dns_digest").toString(),
                        dns01Challenge.getDigest()
                );
                break;
        }
    }

    public String getChallengeType() {
        String challengeType = null;
        if (getParameters().getChallengeType().equalsIgnoreCase(Parameters.CHALLENGE_HTTP01)) {
            challengeType = Http01Challenge.TYPE;
        } else if (getParameters().getChallengeType().equalsIgnoreCase(Parameters.CHALLENGE_DNS01)) {
            challengeType = Dns01Challenge.TYPE;
        }
        return challengeType;
    }


}
