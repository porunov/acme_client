package com.jblur.acme_client;

import org.shredzone.acme4j.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Comparator;

public class CertificateExpireComparator implements Comparator<Certificate> {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateExpireComparator.class);

    @Override
    public int compare(Certificate c1, Certificate c2) {
        long c1expire = c1.getCertificate().getNotAfter().getTime();
        long c2expire = c2.getCertificate().getNotAfter().getTime();

        if (c1expire > c2expire) {
            return -1;
        } else if (c1expire < c2expire) {
            return 1;
        }
        return Integer.compare(c1.hashCode(), c2.hashCode());
    }
}
