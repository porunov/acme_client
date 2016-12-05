package com.jblur.acme_client.command.certificate;

import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.CertificateManager;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.util.LinkedList;
import java.util.List;

public abstract class CertificateCommand extends ACMECommand {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateCommand.class);

    public CertificateCommand(Parameters parameters) throws AccountKeyNotFoundException {
        super(parameters);
    }

    public boolean writeCertificate(CertificateManager certificateManagement, String suffix) {
        boolean error = false;
        try {
            IOManager.writeX509Certificate(certificateManagement.downloadCertificate(),
                    Paths.get(getParameters().getCertDir(), "cert" + suffix + ".pem").toString());
            IOManager.writeX509CertificateChain(certificateManagement.downloadCertificateChain(),
                    Paths.get(getParameters().getCertDir(), "chain" + suffix + ".pem").toString());
            IOManager.writeX509CertificateChain(certificateManagement.downloadFullChainCertificate(),
                    Paths.get(getParameters().getCertDir(), "fullchain" + suffix + ".pem").toString());
        } catch (IOException e) {
            LOG.error("Can not write certificate into dir: " + getParameters().getCertDir(), e);
            error = true;
        } catch (CertificateEncodingException e) {
            LOG.error("Can not write certificate. Encoding exception.", e);
            error = true;
        } catch (AcmeException e) {
            LOG.error("Can not download certificate: " + certificateManagement.getCertificate().getLocation(), e);
            error = true;
        }
        return !error;
    }


    public boolean writeCertificateList(List<Certificate> certificateList) {
        try {
            IOManager.serialize(certificateList,
                    Paths.get(getParameters().getWorkDir(), Parameters.CERTIFICATE_URI_LIST).toString());
        } catch (IOException e) {
            LOG.error("Can not write certificate list to file: " + Paths.get(getParameters().getWorkDir(),
                    Parameters.CERTIFICATE_URI_LIST).toString() + "\n Please check permissions of the file.", e);
            return false;
        }
        return true;
    }


    public List<Certificate> getNotExpiredCertificates() {
        List<Certificate> certificateList = null;
        if (IOManager.isFileExists(Paths.get(getParameters().getWorkDir(), Parameters.CERTIFICATE_URI_LIST).toString())) {
            try {
                List<Certificate> allCertificates = (List<Certificate>) IOManager.deserialize(
                        Paths.get(getParameters().getWorkDir(), Parameters.CERTIFICATE_URI_LIST).toString()
                );
                certificateList = new LinkedList<>();
                for (Certificate certificate : allCertificates) {
                    try {
                        if (certificate.download().getNotAfter().getTime() > System.currentTimeMillis()) {
                            certificate.rebind(getSession());
                            certificateList.add(certificate);
                        }
                    } catch (AcmeException e) {
                        LOG.warn("Can not download a certificate: " + certificate.getLocation(), e);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                LOG.warn("Your file can not be read. It has a bad structure", e);
            }
        }
        return certificateList;
    }


}
