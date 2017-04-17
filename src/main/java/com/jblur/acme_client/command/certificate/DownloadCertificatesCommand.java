package com.jblur.acme_client.command.certificate;

import com.jblur.acme_client.CertificateExpireComparator;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.CertificateManager;
import org.shredzone.acme4j.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeSet;

public class DownloadCertificatesCommand extends CertificateCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadCertificatesCommand.class);

    public DownloadCertificatesCommand(Parameters parameters) throws AccountKeyNotFoundException {
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

        if (getParameters().isNewestOnly()) {
            CertificateManager certificateManagement = new CertificateManager(certificateSet.first());
            error = error || !writeCertificate(certificateManagement, "");
        } else {
            int i = 0;
            for (Certificate certificate : certificateSet) {
                CertificateManager certificateManagement = new CertificateManager(certificate);
                error = error || !writeCertificate(certificateManagement, "_" + i);
                i++;
            }
        }
    }
}
