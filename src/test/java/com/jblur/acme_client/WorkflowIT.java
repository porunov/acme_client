package com.jblur.acme_client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Set;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkflowIT {

    private static final String LOG_DIR = "target/log";
    private static final String WORKING_DIR = "target/working";
    private static final String DIGESTS_DIR = "target/digests";
    private static final String WELL_KNOWN_DIR = "target/www/.well-known/acme-challenge";
    private static final String CERTS_DIR = "target/certs";
    
    private static final String SERVER_URL = "https://acme-staging-v02.api.letsencrypt.org/directory";
    
    private static final String ACCOUNT_KEY_FILE = "target/test-account.key";
    private static final String DOMAIN_KEY_FILE = "target/test-domain.key";
    private static final String DOMAIN_CSR_FILE = "target/test-domain.csr";
    
    private static final Properties PROPS;
    
    private static final String EMAIL_1;
    private static final String EMAIL_2;
    
    private static final String CERT_CN;
    private static final String CERT_ALT_NAME;
    
    static {
        PROPS = load("integration-test.properties");
        
        EMAIL_1 = PROPS.getProperty("email-1");
        EMAIL_2 = PROPS.getProperty("email-2");
        
        CERT_CN = PROPS.getProperty("cert-cn");
        CERT_ALT_NAME = PROPS.getProperty("cert-alt-name");
    }
    
    @BeforeAll
    public static void init() throws IOException {
        
        if (PROPS.isEmpty()) {
            Assertions.fail("Missing file: integration-test.properties");
        }
        
        Files.createDirectories(Paths.get(LOG_DIR));
        Files.createDirectories(Paths.get(WORKING_DIR));
        Files.createDirectories(Paths.get(DIGESTS_DIR));
        Files.createDirectories(Paths.get(WELL_KNOWN_DIR));
        Files.createDirectories(Paths.get(CERTS_DIR));
    }
    
    @AfterEach
    public void sleep() throws InterruptedException
    {
        Thread.sleep(3 * 1000);
    }
    
    @Test
    @Order(1)
    public void generateKeysAndCsr(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        KeyPair accountKeyPair = generateKeyPair(ACCOUNT_KEY_FILE, 2048);
        Assertions.assertNotNull(accountKeyPair);
        
        KeyPair domainKeyPair = generateKeyPair(DOMAIN_KEY_FILE, 2048);
        Assertions.assertNotNull(domainKeyPair);
        
        String distinguishedNames = String.format(CertificateTest.TEST_DN, CERT_CN, EMAIL_1);
        
        PKCS10CertificationRequest csr = generateCSR(domainKeyPair, DOMAIN_CSR_FILE, 
                distinguishedNames, CERT_ALT_NAME);
        
        Set<String> domains = CSRParser.getDomains(csr.getEncoded());
        
        Assertions.assertNotNull(domains);
        Assertions.assertTrue(domains.contains(CERT_CN));
        Assertions.assertTrue(domains.contains(CERT_ALT_NAME));
    }
    
    @Test
    @Order(2)
    public void executeGetAgreementUrl(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "get-agreement-url",
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(3)
    public void executeRegister(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "register",
                "--with-agreement-update",
                "--account-key", ACCOUNT_KEY_FILE,
                "--email", EMAIL_1,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(4)
    public void executeAddEmail(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "add-email",
                "--account-key", ACCOUNT_KEY_FILE,
                "--email", EMAIL_2,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(5)
    public void executeOrderCertificate(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "order-certificate",
                "--account-key", ACCOUNT_KEY_FILE,
                "--csr", DOMAIN_CSR_FILE,
                "--challenge-type", "DNS01",
                "--dns-digests-dir", DIGESTS_DIR,
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(6)
    public void executeDownloadChallenges(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "download-challenges",
                "--account-key", ACCOUNT_KEY_FILE,
                "--csr", DOMAIN_CSR_FILE,
                "--challenge-type", "DNS01",
                "--dns-digests-dir", DIGESTS_DIR,
                "--well-known-dir", WELL_KNOWN_DIR,
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(7)
    public void executeHttpPost(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        final String wildcardPrefix = "*.";
        
        String commonName = CERT_CN.startsWith(wildcardPrefix) ? 
                CERT_CN.substring(wildcardPrefix.length(), CERT_CN.length()) : CERT_CN;
        
        String altName = CERT_ALT_NAME.startsWith(wildcardPrefix) ? 
                CERT_ALT_NAME.substring(wildcardPrefix.length(), CERT_ALT_NAME.length()) : CERT_ALT_NAME;
        
        Application.main(new String[] {"--command", "http-post",
                "--csr", DOMAIN_CSR_FILE,
                "--dns-digests-dir", DIGESTS_DIR,
                "--ddns-url", PROPS.getProperty("ddns-url"),
                "--ddns-pause-millis", "3000",
                "--ddns-host-key", PROPS.getProperty("ddns-host-key"),
                "--ddns-record-key", PROPS.getProperty("ddns-record-key"),
                "--ddns-token-key", PROPS.getProperty("ddns-token-key"),
                "--domain-tokens", 
                        "_acme-challenge." + commonName + "=" + PROPS.getProperty("ddns-token-cert-cn") + ",", 
                        "_acme-challenge." + altName + "=" + PROPS.getProperty("ddns-token-cert-alt-name"),
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
        
        // Wait 5 minutes for DNS propagation. 
        Thread.sleep(5 * 60 * 1000);
    }
    
    @Test
    @Order(8)
    public void executeVerifyDomains(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "verify-domains",
                "--account-key", ACCOUNT_KEY_FILE,
                "--csr", DOMAIN_CSR_FILE,
                "--challenge-type", "DNS01",
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(9)
    public void executeGenerateCertificate(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "generate-certificate",
                "--account-key", ACCOUNT_KEY_FILE,
                "--csr", DOMAIN_CSR_FILE,
                "--cert-dir", CERTS_DIR,
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(10)
    public void executeDownloadCertificates(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "download-certificates",
                "--account-key", ACCOUNT_KEY_FILE,
                "--cert-dir", CERTS_DIR,
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(11)
    public void executeRevokeCertificate(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "revoke-certificate",
                "--account-key", ACCOUNT_KEY_FILE,
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(12)
    public void executeDeactivateDomainAuthorization(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "deactivate-domain-authorization",
                "--account-key", ACCOUNT_KEY_FILE,
                "--work-dir", WORKING_DIR,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }
    
    @Test
    @Order(13)
    public void executeDeactivateAccount(final TestInfo testInfo) throws Exception {
        
        System.out.println(testInfo.getTestMethod().get().getName());
        
        Application.main(new String[] {"--command", "deactivate-account",
                "--account-key", ACCOUNT_KEY_FILE,
                "--log-dir", LOG_DIR,
                "--server-url", SERVER_URL});
    }

    private KeyPair generateKeyPair(final String fileName, final int keysize) 
            throws IOException, NoSuchAlgorithmException {
        
        KeyPair keyPair = CertificateTest.readKeyPair(fileName);
        
        if (keyPair == null) {
            keyPair = CertificateTest.createKeyPair(keysize);
            CertificateTest.writeKeyPair(fileName, keyPair);
        }
        
        return keyPair;
    }

    private PKCS10CertificationRequest generateCSR(final KeyPair keyPair, final String fileName, 
            final String distinguishedNames, final String... altNames) 
            throws IOException, OperatorCreationException {
        
        PKCS10CertificationRequest csr = CertificateTest.readCSR(fileName);
        
        if (csr == null) {
            csr = CertificateTest.createCSR(keyPair, distinguishedNames, altNames);
            CertificateTest.writeCSR(fileName, csr);
        }
        
        return csr;
    }
    
    private static Properties load(final String fileName) {
        
        Properties properties = new Properties();
        
        File propertyFile = new File(fileName);
        if (propertyFile.isFile()) {
            try (FileReader reader = new FileReader(propertyFile)) {
                properties.load(reader);
            }
            catch (IOException ex) {
                // Ignore.
            }
        }
        
        return properties;
    }
}
