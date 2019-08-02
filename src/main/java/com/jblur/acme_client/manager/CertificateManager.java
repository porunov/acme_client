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
import java.util.Arrays;
import java.util.List;

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

        X509Certificate[] fullChain = downloadFullChainCertificate();

        if(fullChain.length==0){
            return fullChain;
        }

        if (fullChain.length==1){
            return new X509Certificate[0];
        }

        X509Certificate[] chain = new X509Certificate[fullChain.length-1];

        System.arraycopy(fullChain, 1, chain, 0, fullChain.length-1);

        return chain;
    }

    public X509Certificate[] downloadFullChainCertificate() {
        return this.certificate.getCertificateChain().toArray(new X509Certificate[]{});
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
