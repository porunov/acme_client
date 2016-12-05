package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Registration;
import org.shredzone.acme4j.RevocationReason;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.cert.X509Certificate;

public class CertificateManager {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateManager.class);

    private Certificate certificate;

    //For new certificates and for renewal
    public CertificateManager(byte[] csr, Registration registration) throws AcmeException {
        this.certificate = registration.requestCertificate(csr);
    }

    //For existing certificate
    public CertificateManager(Session session, URI certificateLocationUri) {
        this.certificate = Certificate.bind(session, certificateLocationUri);
    }

    public CertificateManager(Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateManager(Certificate certificate, Session session) {
        this.certificate = certificate;
        try {
            certificate.rebind(session);
        } catch (Exception ex) {
            LOG.warn("Can not rebind certificate: " + certificate.getLocation() + " to session: " +
                    session.getServerUri().toString(), ex);
        }
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public X509Certificate downloadCertificate() throws AcmeException {
        return this.certificate.download();
    }

    public X509Certificate[] downloadCertificateChain() throws AcmeException {
        return this.certificate.downloadChain();
    }

    public X509Certificate[] downloadFullChainCertificate() throws AcmeException {
        X509Certificate cert = downloadCertificate();
        X509Certificate[] chain = downloadCertificateChain();
        X509Certificate[] fullChain = new X509Certificate[chain.length + 1];
        fullChain[0] = cert;
        int i = 1;
        for (X509Certificate x509Certificate : chain) {
            fullChain[i] = x509Certificate;
            i++;
        }
        return fullChain;
    }

    public void revokeCertificate() throws AcmeException {
        this.certificate.revoke();
    }

    public boolean revokeCertificate(int leftSeconds) throws AcmeException {
        if ((System.currentTimeMillis() + leftSeconds) >= this.certificate.download().getNotAfter().getTime()) {
            this.certificate.revoke();
            return true;
        }
        return false;
    }

    public boolean revokeCertificate(X509Certificate x509Certificate, int leftSeconds) throws AcmeException {
        if ((System.currentTimeMillis() + leftSeconds) >= x509Certificate.getNotAfter().getTime()) {
            this.certificate.revoke();
            return true;
        }
        return false;
    }

    public void revokeCertificate(RevocationReason revocationReason) throws AcmeException {
        this.certificate.revoke(revocationReason);
    }

    public long getExpirationDate() throws AcmeException {
        return certificate.download().getNotAfter().getTime();
    }

    public long getExpirationDate(X509Certificate x509Certificate) throws AcmeException {
        return x509Certificate.getNotAfter().getTime();
    }

}
