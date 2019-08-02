package com.jblur.acme_client.command.certificate;

import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.CSRParser;
import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.command.AuthorizedCommand;
import com.jblur.acme_client.manager.AuthorizationManager;
import com.jblur.acme_client.manager.CertificateManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

abstract class CertificateCommand extends AuthorizedCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateCommand.class);

    private static Type urlsListTokenType = new TypeToken<List<String>>(){}.getType();

    final String ORDER_FILE_PATH;
    private final String CERTIFICATE_FILE_PATH;

    CertificateCommand(Parameters parameters) throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
        ORDER_FILE_PATH = Paths.get(getParameters().getWorkDir(), Parameters.ORDER_URI_LIST).toString();
        CERTIFICATE_FILE_PATH = Paths.get(getParameters().getWorkDir(), Parameters.CERTIFICATE_URI_LIST).toString();
    }

    List<Order> getNotExpiredOrders() {
        List<Order> oldOrderList = new LinkedList<>();

        if (!IOManager.isFileExists(ORDER_FILE_PATH)) {
            return null;
        }

        List<String> orderLocationList;

        try {
            orderLocationList = getGson().fromJson(
                    IOManager.readString(ORDER_FILE_PATH),
                    urlsListTokenType);
        } catch (Exception e) {
            LOG.warn("Your file cannot be read. It has a bad structure", e);
            return null;
        }

        for(String orderLocation : orderLocationList){
            try {
                oldOrderList.add(getAccountManager().getLogin().bindOrder(new URL(orderLocation)));
            } catch (MalformedURLException e) {
                LOG.warn("URL isn't correct: "+orderLocation, e);
            } catch (Exception e){
                LOG.warn("Cannot retrieve order: "+orderLocation, e);
            }
        }

        List<Order> orderList = new LinkedList<>();

        for (Order order : oldOrderList) {
            if(order == null){
                LOG.warn("Found NULL order in the file " + ORDER_FILE_PATH);
                continue;
            }
            try {
                if (order.getExpires().isAfter(Instant.now())) {
                    orderList.add(order);
                }
            } catch (Exception e) {
                LOG.warn("Order "+order.getLocation().toString()+" can not be rebinded. " +
                        "Please check internet connectivity or authorization existence.", e);
                orderList.add(order);
            }
        }

        return orderList;
    }


    boolean writeOrderList(List<Order> orderList) {
        try {
            List<String> orderLocationList = new LinkedList<>();

            for(Order order : orderList){
                orderLocationList.add(order.getLocation().toString());
            }

            IOManager.writeString(ORDER_FILE_PATH,
                    getGson().toJson(orderLocationList, urlsListTokenType));
        } catch (IOException e) {
            LOG.error("Cannot write order list to file: " + ORDER_FILE_PATH
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
                    String subdir = authorizationManagement.getAuthorization().getIdentifier().getDomain()+
                            returnIfWildcard(authorizationManagement.getAuthorization());
                    path = Paths.get(getParameters().getWellKnownDir(), subdir).toString();
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
                Authorization authorization = authorizationManagement.getAuthorization();
                String fileSuffix = "_dns_digest"+returnIfWildcard(authorization);
                IOManager.writeString(
                        Paths.get(getParameters().getDnsDigestDir(),
                                authorizationManagement.getAuthorization().getIdentifier().getDomain() + fileSuffix).toString(),
                        dns01Challenge.getDigest()
                );
                break;
        }
    }

    public Set<String> retrieveDomainsFromParametersOrCSR(){
        if(getParameters().getDomains()!=null){
            return getParameters().getDomains();
        }
        if(getParameters().getCsr() != null){
            try {
                return CSRParser.getDomains(IOManager.readCSR(getParameters().getCsr()));
            } catch (IOException e) {
                LOG.error("Cannot read file: " + getParameters().getCsr(), e);
                error = true;
            }
        }
        return null;
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

    boolean writeCertificate(CertificateManager certificateManagement, String suffix) {
        boolean error = false;
        try {
            IOManager.writeX509Certificate(certificateManagement.downloadCertificate(),
                    Paths.get(getParameters().getCertDir(), "cert" + suffix + ".pem").toString());
            IOManager.writeX509CertificateChain(certificateManagement.downloadCertificateChain(),
                    Paths.get(getParameters().getCertDir(), "chain" + suffix + ".pem").toString());
            IOManager.writeX509CertificateChain(certificateManagement.downloadFullChainCertificate(),
                    Paths.get(getParameters().getCertDir(), "fullchain" + suffix + ".pem").toString());
        } catch (IOException e) {
            LOG.error("Cannot write certificate into dir: " + getParameters().getCertDir(), e);
            error = true;
        } catch (CertificateEncodingException e) {
            LOG.error("Cannot write certificate. Encoding exception.", e);
            error = true;
        }
        return !error;
    }


    boolean writeCertificateList(List<Certificate> certificateList) {
        try {
            List<String> certificateLocationList = new LinkedList<>();
            for(Certificate certificate : certificateList){
                certificateLocationList.add(certificate.getLocation().toString());
            }
            IOManager.writeString(CERTIFICATE_FILE_PATH,
                    getGson().toJson(certificateLocationList, urlsListTokenType));
        } catch (IOException e) {
            LOG.error("Cannot write certificate list to file: " + Paths.get(getParameters().getWorkDir(),
                    Parameters.CERTIFICATE_URI_LIST).toString() + "\n Please check permissions of the file.", e);
            return false;
        }
        return true;
    }

    List<Certificate> getNotExpiredCertificates() {
        List<Certificate> oldCertificateList = new LinkedList<>();

        if (!IOManager.isFileExists(CERTIFICATE_FILE_PATH)) {
            return null;
        }

        List<String> certificateLocationList;
        try {
            certificateLocationList = getGson().fromJson(
                    IOManager.readString(CERTIFICATE_FILE_PATH), urlsListTokenType);
        } catch (Exception e) {
            LOG.warn("Your file cannot be read. It has a bad structure", e);
            return null;
        }

        for(String certificateLocation : certificateLocationList){
            try {
                oldCertificateList.add(getAccountManager().getLogin().bindCertificate(new URL(certificateLocation)));
            } catch (MalformedURLException e) {
                LOG.warn("URL isn't correct: "+certificateLocation, e);
            } catch (Exception e){
                LOG.warn("Cannot retrieve certificate: "+certificateLocation, e);
            }
        }

        List<Certificate> certificateList = new LinkedList<>();

        for (Certificate certificate : oldCertificateList) {
            if(certificate == null){
                LOG.warn("Found NULL certificate in the file " + CERTIFICATE_FILE_PATH+". Remove NULL certificate.");
                continue;
            }
            try {
                if (certificate.getCertificate().getNotAfter().getTime() > System.currentTimeMillis()) {
                    certificateList.add(certificate);
                }
            } catch (Exception e) {
                LOG.warn("Certificate "+certificate.getLocation().toString()+" cannot be rebinded. " +
                        "Please check internet connectivity or certificate existence.", e);
                certificateList.add(certificate);
            }
        }

        return certificateList;
    }

    String getDomain(Authorization authorization){
        String domain = authorization.getIdentifier().getDomain();
        if(authorization.isWildcard() && !domain.startsWith("*.")){
            domain = "*."+domain;
        }
        return domain;
    }

    private String returnIfWildcard(Authorization authorization){
        return authorization.isWildcard()?"_wildcard":"";
    }

}
