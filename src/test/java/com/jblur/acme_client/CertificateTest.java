package com.jblur.acme_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.util.CertificateUtils;
import org.shredzone.acme4j.util.KeyPairUtils;

public class CertificateTest {

    public static final String TEST_DN = 
            "C=GB, ST=England, L=London, O=Organisation, OU=Organisational Unit, CN=%s, EMAILADDRESS=%s";
    
    @Test
    public void createKeyPair() throws Exception {
        
        KeyPair domainKeyPair = CertificateTest.createKeyPair(2048);
        Assertions.assertNotNull(domainKeyPair);
    }
    
    @Test
    public void createCSR() throws Exception {
        
        KeyPair domainKeyPair = CertificateTest.createKeyPair(2048);
        Assertions.assertNotNull(domainKeyPair);
        
        PKCS10CertificationRequest csr = CertificateTest.createCSR(domainKeyPair, 
                String.format(TEST_DN, "www.example.com", "admin@example.com"));
        
        Set<String> domains = CSRParser.getDomains(csr.getEncoded());
        
        Assertions.assertNotNull(domains);
        Assertions.assertTrue(domains.contains("www.example.com"));
    }
    
    @Test
    public void createCSRwithSAN() throws Exception {
        
        KeyPair domainKeyPair = CertificateTest.createKeyPair(2048);
        Assertions.assertNotNull(domainKeyPair);
        
        PKCS10CertificationRequest csr = CertificateTest.createCSR(domainKeyPair,
                String.format(TEST_DN, "www.example.com", "admin@example.com"),
                "san-a.example.com", "san-b.example.com");
        
        Set<String> domains = CSRParser.getDomains(csr.getEncoded());
        
        Assertions.assertNotNull(domains);
        Assertions.assertTrue(domains.contains("www.example.com"));
        Assertions.assertTrue(domains.contains("san-a.example.com"));
        Assertions.assertTrue(domains.contains("san-b.example.com"));
    }
    
    public static KeyPair readKeyPair(final String fileName) throws IOException {
        
        if (new File(fileName).exists())
        {
            try (FileReader reader = new FileReader(fileName)) {
                KeyPair keyPair = KeyPairUtils.readKeyPair(reader);
                return keyPair;
            }
        }
        
        return null;
    }
    
    public static void writeKeyPair(final String fileName, final KeyPair keyPair) throws IOException {
        
        JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(new FileWriter(fileName));
        jcaPEMWriter.writeObject(keyPair.getPrivate());
        jcaPEMWriter.close();
        
        jcaPEMWriter = new JcaPEMWriter(new FileWriter(fileName + ".pub"));
        jcaPEMWriter.writeObject(keyPair.getPublic());
        jcaPEMWriter.close();
    }
    
    public static KeyPair createKeyPair(int keysize) 
            throws NoSuchAlgorithmException {
        
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keysize);
        
        KeyPair keyPair = generator.generateKeyPair();
        
        return keyPair;
    }
    
    public static PKCS10CertificationRequest readCSR(final String fileName) throws IOException {
        
        if (new File(fileName).exists())
        {
            try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
                PKCS10CertificationRequest p10Object = CertificateUtils.readCSR(fileInputStream);
                return p10Object;
            }
        }
        
        return null;
    }
    
    public static void writeCSR(final String fileName, final PKCS10CertificationRequest csr) throws IOException {
        
        JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(new FileWriter(fileName));
        jcaPEMWriter.writeObject(csr);
        jcaPEMWriter.close();
    }
    
    public static PKCS10CertificationRequest createCSR(final KeyPair keyPair, final String distinguishedNames, 
            final String... altNames) 
            throws IOException, OperatorCreationException {
        
        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal(distinguishedNames), keyPair.getPublic());
        
        if (altNames != null && altNames.length > 0) {
            
            ASN1Encodable[] subjectAltNames = new ASN1Encodable[altNames.length];
            
            for (int index = 0; index < altNames.length; index++) {
                subjectAltNames[index] = new GeneralName(GeneralName.dNSName, altNames[index]);
            }
            
            DERSequence subjectAltNamesExtension = new DERSequence(subjectAltNames);
            
            ExtensionsGenerator extGen = new ExtensionsGenerator();
            extGen.addExtension(Extension.subjectAlternativeName, false, subjectAltNamesExtension);
            
            p10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate());
        }
        
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = csBuilder.build(keyPair.getPrivate());
        PKCS10CertificationRequest csr = p10Builder.build(signer);
        
        return csr;
    }

}
