package com.jblur.acme_client;

import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Comparator;

public class CertificateExpireComparator implements Comparator<Certificate> {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateExpireComparator.class);

    @Override
    public int compare(Certificate c1, Certificate c2) {
        long c1expire = Long.MIN_VALUE, c2expire = Long.MIN_VALUE;
        try {
            c1expire = c1.download().getNotAfter().getTime();
        } catch (AcmeException e) {
            LOG.warn("Cannot fetch x509 cert from certificate: " + c1.getLocation().toString(), e);
        }

        try {
            c2expire = c2.download().getNotAfter().getTime();
        } catch (AcmeException e) {
            LOG.warn("Cannot fetch x509 cert from certificate: " + c2.getLocation().toString(), e);
        }

        if (c1expire > c2expire) {
            return -1;
        } else if (c1expire < c2expire) {
            return 1;
        }
        return 0;
    }
}
