package com.jblur.acme_client.command.certificate;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.CertificateExpireComparator;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.CertificateManager;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class DownloadCertificatesCommand extends CertificateCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadCertificatesCommand.class);

    public DownloadCertificatesCommand(Parameters parameters) throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        TreeSet<Certificate> certificateSet = new TreeSet<>(new CertificateExpireComparator());

        List<Certificate> savedCertificateList = getNotExpiredCertificates();
        if (savedCertificateList == null || savedCertificateList.size() == 0) {
            LOG.error("Cannot download certificates. Either you haven't generated one or they already expired.");
            error = true;
            return;
        }

        certificateSet.addAll(savedCertificateList);

        List<String> failedCertificates = new LinkedList<>();

        boolean writeCertificateError;

        if (getParameters().isNewestOnly()) {
            CertificateManager certificateManagement = new CertificateManager(certificateSet.first());
            writeCertificateError = !writeCertificate(certificateManagement, "");
            error = error || writeCertificateError;
            if(writeCertificateError) {
                failedCertificates.add(certificateManagement.getCertificate().getLocation().toString());
            }
        } else {
            int i = 0;
            for (Certificate certificate : certificateSet) {
                CertificateManager certificateManagement = new CertificateManager(certificate);
                writeCertificateError = !writeCertificate(certificateManagement, "_" + i);
                error = error || writeCertificateError;
                if(writeCertificateError) {
                    failedCertificates.add(certificateManagement.getCertificate().getLocation().toString());
                }
                i++;
            }
        }

        if (failedCertificates.size() > 0) {
            JsonElement failedCertificatesJsonElement = getGson().toJsonTree(failedCertificates,
                    new TypeToken<List<String>>() {}.getType());
            result.add("failed_certificates", failedCertificatesJsonElement);
            error=true;
        }

    }
}
