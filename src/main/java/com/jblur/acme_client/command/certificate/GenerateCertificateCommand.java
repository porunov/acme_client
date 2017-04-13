package com.jblur.acme_client.command.certificate;

import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.CertificateManager;
import com.jblur.acme_client.manager.RegistrationManager;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class GenerateCertificateCommand extends CertificateCommand {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateCertificateCommand.class);

    private RegistrationManager registrationManager;

    public GenerateCertificateCommand(Parameters parameters, RegistrationManager registrationManager)
            throws AccountKeyNotFoundException {
        super(parameters);
        this.registrationManager = registrationManager;
    }

    @Override
    public void commandExecution() {
        try {
            List<Certificate> certificateList = new LinkedList<>();
            CertificateManager certificateManagement = new CertificateManager(
                    IOManager.readCSR(getParameters().getCsr()),
                    registrationManager.getRegistration());
            certificateList.add(certificateManagement.getCertificate());
            writeCertificate(certificateManagement, "");

            List<Certificate> savedCertificateList = getNotExpiredCertificates();
            if (savedCertificateList != null) {
                certificateList.addAll(savedCertificateList);
            }

            error = error || !writeCertificateList(certificateList);
        } catch (IOException e) {
            LOG.error("Cannot read csr: " + getParameters().getCsr(), e);
            error = true;
        } catch (AcmeException e) {
            LOG.error("Cannot get certificate. Check if your domains of the certificate are verified", e);
            error = true;
        }
    }
}
