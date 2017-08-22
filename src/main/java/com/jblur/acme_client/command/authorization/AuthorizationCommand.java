package com.jblur.acme_client.command.authorization;

import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

abstract class AuthorizationCommand extends ACMECommand {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationCommand.class);

    private static Type listOfAuthorizationLocationsObject = new TypeToken<List<String>>(){}.getType();

    final String AUTHORIZATION_FILE_PATH;

    AuthorizationCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
        AUTHORIZATION_FILE_PATH = Paths.get(getParameters().getWorkDir(), Parameters.AUTHORIZATION_URI_LIST).toString();
    }

    List<Authorization> getNotExpiredAuthorizations() {
        List<Authorization> oldAuthorizationList = new LinkedList<>();

        if (!IOManager.isFileExists(AUTHORIZATION_FILE_PATH)) {
            return null;
        }

        List<String> authorizationLocationList;

        try {
            authorizationLocationList = getGson().fromJson(
                    IOManager.readString(AUTHORIZATION_FILE_PATH),
                    listOfAuthorizationLocationsObject);
        } catch (Exception e) {
            LOG.warn("Your file cannot be read. It has a bad structure", e);
            return null;
        }

        for(String authorizationLocation : authorizationLocationList){
            try {
                oldAuthorizationList.add(Authorization.bind(getSession(), new URI(authorizationLocation)));
            } catch (URISyntaxException e) {
                LOG.warn("URI isn't correct: "+authorizationLocation, e);
            } catch (Exception e){
                LOG.warn("Cannot retrieve authorization: "+authorizationLocation, e);
            }
        }

        List<Authorization> authorizationList = new LinkedList<>();

        for (Authorization authorization : oldAuthorizationList) {
            try {
                if (authorization.getExpires().isAfter(Instant.now())) {
                    authorizationList.add(authorization);
                }
            } catch (NullPointerException e){
                LOG.warn("Found NULL authorization in the file " +
                        AUTHORIZATION_FILE_PATH, e);
            } catch (Exception e) {
                LOG.warn("Authorization "+authorization.getLocation().toString()+" can not be rebinded. " +
                        "Please check internet connectivity or authorization existence.", e);
                authorizationList.add(authorization);
            }
        }

        return authorizationList;
    }


    boolean writeAuthorizationList(List<Authorization> authorizationList) {
        try {
            List<String> authorizationLocationList = new LinkedList<>();

            for(Authorization authorization : authorizationList){
                authorizationLocationList.add(authorization.getLocation().toString());
            }

            IOManager.writeString(AUTHORIZATION_FILE_PATH,
                    getGson().toJson(authorizationLocationList, listOfAuthorizationLocationsObject));
        } catch (IOException e) {
            LOG.error("Cannot write authorization list to file: " + AUTHORIZATION_FILE_PATH
                    + "\n Please check permissions of the file.", e);
            return false;
        }
        return true;
    }

    void writeChallengeByAuthorization(AuthorizationManager authorizationManagement) throws Exception {
        switch (getChallengeType()) {
            case Http01Challenge.TYPE:
                Http01Challenge http01Challenge = authorizationManagement.getHttp01Challenge();
                if(http01Challenge.getStatus()== Status.INVALID){
                    throw new ChallengeInvalidException(http01Challenge.getLocation().toString());
                }
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
                if(dns01Challenge.getStatus()== Status.INVALID){
                    throw new ChallengeInvalidException(dns01Challenge.getLocation().toString());
                }
                IOManager.writeString(
                        Paths.get(getParameters().getDnsDigestDir(),
                                authorizationManagement.getAuthorization().getDomain() + "_dns_digest").toString(),
                        dns01Challenge.getDigest()
                );
                break;
        }
    }

    HashSet<String> getDomains(List<Authorization> authorizationList){
        HashSet<String> domains;
        if(getParameters().getDomains() == null){
            domains = new HashSet<>();
            for (Authorization authorization : authorizationList) {
                try {
                    domains.add(authorization.getDomain());
                }catch (Exception e){
                    LOG.warn("Cannot retrieve domain from authorization: "+authorization.getLocation(), e);
                }
            }
        }else {
            domains = new HashSet<>(getParameters().getDomains());
        }
        return domains;
    }

    String getChallengeType() {
        String challengeType = null;
        if (getParameters().getChallengeType().equalsIgnoreCase(Parameters.CHALLENGE_HTTP01)) {
            challengeType = Http01Challenge.TYPE;
        } else if (getParameters().getChallengeType().equalsIgnoreCase(Parameters.CHALLENGE_DNS01)) {
            challengeType = Dns01Challenge.TYPE;
        }
        return challengeType;
    }


}
