package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.RevocationReason;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class CertificateManager {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateManager.class);

    private Certificate certificate;

    public CertificateManager(Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateManager(Login login, URL certificateURL) {
        this.certificate = login.bindCertificate(certificateURL);
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public X509Certificate downloadCertificate() {
        return this.certificate.getCertificate();
    }

    public X509Certificate[] downloadCertificateChain() {
        return this.certificate.getCertificateChain().toArray(new X509Certificate[]{});
    }

    public X509Certificate[] downloadFullChainCertificate() {
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
        if ((System.currentTimeMillis() + leftSeconds) >= this.certificate.getCertificate().getNotAfter().getTime()) {
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

    public static void revokeCertificate(Session session, KeyPair domainKeyPair, X509Certificate cert, RevocationReason reason)
            throws AcmeException {
        Certificate.revoke(session, domainKeyPair, cert, reason);
    }

    public long getExpirationDate() throws AcmeException {
        return certificate.getCertificate().getNotAfter().getTime();
    }

    public long getExpirationDate(X509Certificate x509Certificate) {
        return x509Certificate.getNotAfter().getTime();
    }

}
