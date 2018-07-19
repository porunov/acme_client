package com.jblur.acme_client;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CSRParser {
    private final static String COMMON_NAME = "2.5.4.3";

    private final static String EXTENSION_REQUEST = "1.2.840.113549.1.9.14";
    private final static ASN1ObjectIdentifier EXTENSION_REQUEST_IDENTIFIER
            = new ASN1ObjectIdentifier(EXTENSION_REQUEST);

    private final static String SUBJECT_ALTERNATIVE_NAME = "2.5.29.17";

    public static Set<String> getDomains(byte[] csrBytes) throws IOException {

        Set<String> domains = new HashSet<>();

        JcaPKCS10CertificationRequest p10Object = new JcaPKCS10CertificationRequest(csrBytes);

        for(RDN rdn : p10Object.getSubject().getRDNs()){
            String id = rdn.getFirst().getType().getId();
            if(COMMON_NAME.equals(id)){
                String commonName = rdn.getFirst().getValue().toString();
                domains.add(commonName);
            }
        }

        for(Attribute attribute : p10Object.getAttributes(EXTENSION_REQUEST_IDENTIFIER)){

            Iterator<ASN1Encodable> attrValIt = attribute.getAttrValues().iterator();
            while (attrValIt.hasNext()){

                Iterator<ASN1Encodable> seqIt = ((DERSequence) attrValIt.next()).iterator();

                while (seqIt.hasNext()){
                    DERSequence seq = (DERSequence) seqIt.next();
                    ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) seq.getObjectAt(0);
                    if (SUBJECT_ALTERNATIVE_NAME.equals(oid.getId())) {
                        DEROctetString str = (DEROctetString) seq.getObjectAt(1);

                        GeneralNames names = GeneralNames.getInstance(str.getOctets());

                        for(String domain : names.toString().split("\\s+")) {
                            if(!domain.endsWith(":")){
                                domains.add(domain);
                            }
                        }

                    }
                }

            }

        }

        return domains;
    }
}
