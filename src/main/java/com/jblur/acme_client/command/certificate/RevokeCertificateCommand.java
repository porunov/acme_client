package com.jblur.acme_client.command.certificate;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.List;

public class RevokeCertificateCommand extends CertificateCommand {
    private static final Logger LOG = LoggerFactory.getLogger(RevokeCertificateCommand.class);

    public RevokeCertificateCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        List<Certificate> certificatesList = getNotExpiredCertificates();
        if (certificatesList == null || certificatesList.size() == 0) {
            LOG.error("Cannot revoke certificates. Either you haven't generated one or they already expired.");
            error = true;
            return;
        }

        List<Certificate> newCertificatesList = new LinkedList<>();

        List<String> failedCertificates = new LinkedList<>();

        for (Certificate certificate : certificatesList) {
            try {
                if (certificate.download().getNotBefore().getTime() > getParameters().getFromTime() &&
                        certificate.download().getNotAfter().getTime() < getParameters().getToTime()) {
                    try {
                        certificate.revoke();
                    } catch (Exception e) {
                        LOG.warn("Cannot revoke certificate: " + certificate.getLocation(), e);
                        failedCertificates.add(certificate.getLocation().toString());
                        error = true;
                    }
                } else if (certificate.download().getNotAfter().getTime() > System.currentTimeMillis()) {
                    newCertificatesList.add(certificate);
                }
            } catch (AcmeException e) {
                LOG.error("Cannot check certificate: " + certificate.getLocation(), e);
            }
        }
        error = error | !writeCertificateList(newCertificatesList);

        if (failedCertificates.size() > 0) {
            JsonElement failedCertificatesJsonElement = getGson().toJsonTree(failedCertificates,
                    new TypeToken<List<String>>() {}.getType());
            result.add("failed_certificates", failedCertificatesJsonElement);
            error=true;
        }

    }

}
