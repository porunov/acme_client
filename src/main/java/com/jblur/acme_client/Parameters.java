package com.jblur.acme_client;

import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Parameters {

    public final static String COMMAND_REGISTER = "register";
    public final static String COMMAND_GET_AGREEMENT_URL = "get-agreement-url";
    public final static String COMMAND_ADD_EMAIL = "add-email";
    public final static String COMMAND_DEACTIVATE_ACCOUNT = "deactivate-account";
    public final static String COMMAND_ORDER_CERTIFICATE = "order-certificate";
    public final static String COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION = "deactivate-domain-authorization";
    public final static String COMMAND_DOWNLOAD_CHALLENGES = "download-challenges";
    public final static String COMMAND_VERIFY_DOMAINS = "verify-domains";
    public final static String COMMAND_GENERATE_CERTIFICATE = "generate-certificate";
    public final static String COMMAND_DOWNLOAD_CERTIFICATES = "download-certificates";
    public final static String COMMAND_REVOKE_CERTIFICATE = "revoke-certificate";
    public final static String COMMAND_HTTP_GET = "http-get";
    public final static String COMMAND_HTTP_POST = "http-post";

    public final static String ORDER_URI_LIST = "order_uri_list";
    public final static String CERTIFICATE_URI_LIST = "certificate_uri_list";
    public final static String CHALLENGE_HTTP01 = "HTTP01";
    public final static String CHALLENGE_DNS01 = "DNS01";
    public final static StringBuilder MAIN_USAGE = new StringBuilder();
    private static final Logger LOG = LoggerFactory.getLogger(Parameters.class);
    private final static int COLUMN_SIZE = 80;
    private final static int INDENT_NUM = 6;
    
    /**
    * Generate indent string.
    */
    private static String generateIndentString(int length) {
        return new String(new char[length]).replace('\0', ' ');
    }

    /**
    * Wrap a potentially long line to {COLUMN_SIZE}.
    */
    private static String wrapString(String s) {
        int length = COLUMN_SIZE;
        StringBuilder sb = new StringBuilder(s);
        int i = 0;
        while (i + length < sb.length() && (i = sb.lastIndexOf(" ", i + length)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        sb.append("\n");
        return sb.toString();
    }    

    /**
    * Format parameters display.
    * 
    * Input: String: Parameter names separated with comma;
    *        Boolean: true for required parameters, false for optional parameters.
    */
    private static String formatParameter(String p, boolean required) {
        int length = COLUMN_SIZE;
        StringBuilder fp = new StringBuilder();
        fp.append(generateIndentString(INDENT_NUM));
        fp.append((required)?"Required ":"Optional ");
        fp.append((p.contains(","))?"parameters: ":"parameter : ");
        int fplenght = fp.length();
        fp.append(p);
        int i = 0;
        while (i + length < fp.length() && (i = fp.lastIndexOf(" ", i + length)) != -1) {
            fp.replace(i, i + 1, "\n"+generateIndentString(fplenght));
        }
        fp.append("\n");
        return fp.toString();
    }    

    /*
     Additional usage information to print to STD_OUT with command line parameter '--help'.
    */
    static {
        MAIN_USAGE.append("---Running the application---\n\n");
        MAIN_USAGE.append(wrapString("Either a command (--command value), --help or --version must be "
                + "specified."));
        MAIN_USAGE.append(wrapString("With commands, additional required parameters need to be specified. "
                + "Most commands also support optional parameters. The following optional parameters "
                + "can be used with all commands:"));
        MAIN_USAGE.append(wrapString("--log-dir, --log-level, --server-url, --with-agreement-update"));
        
        MAIN_USAGE.append("\nSyntax: java -jar acme_client <--command value <--option value <...>>\n"
                + "          [--option value [...]] | --help | --version>\n\n");
        MAIN_USAGE.append(wrapString("The application returns a JSON object which contains either "
                + "\"status\":\"ok\" or \"status\":\"error\", where sometimes additional information is provided. "
                + "Detailed information about operations and errors is written to the log file."));
        
        MAIN_USAGE.append("\nWARNING:\nBy default acme_client uses Let's Encrypt's production server:\n"
                + "https://acme-v02.api.letsencrypt.org/directory"
                + "\nIf you want to test the client, use a test server to avoid hitting rate limits:\n"
                + "--server-url https://acme-staging-v02.api.letsencrypt.org/directory\n");
        
        MAIN_USAGE.append("\nCommands:\n");
        MAIN_USAGE.append("\n* "+COMMAND_ADD_EMAIL+"\n"+wrapString("Add an e-mail address to your account "
                + "if it isn't already added to your account.")+formatParameter("--account-key, "
                + "--email",true));
        MAIN_USAGE.append("\n* "+COMMAND_DEACTIVATE_ACCOUNT+"\n"+wrapString("Deactivate the account associated "
                + "with the specified user account key.")+formatParameter("--account-key",true));
        MAIN_USAGE.append("\n* "+COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION+"\n"+wrapString("Deactivate all domain "
                + "authorizations for all or specific domains. Useful if you want to remove/sell one or more "
                + "domains.")
                + formatParameter("--account-key",true)
                + formatParameter("--domain or --csr, --work-dir",false)
                + generateIndentString(INDENT_NUM)+"Needs work-dir file: order_uri_list\n");
        MAIN_USAGE.append("\n* "+COMMAND_DOWNLOAD_CERTIFICATES+"\n"+wrapString("Download previously generated "
                + "certificates. By default, all existing certificates are downloaded, sorted ascending by "
                + "expiration date (cert_0.pem being the most recent certificate). Use newest-only to download "
                + "only the most recent certificate.")
                + formatParameter("--account-key",true)
                + formatParameter("--cert-dir, --newest-only, --work-dir",false)
                + generateIndentString(INDENT_NUM)+"Needs work-dir file: certificate_uri_list\n");
        MAIN_USAGE.append("\n* "+COMMAND_DOWNLOAD_CHALLENGES+"\n"+wrapString("Download challenges for previously "
                + "generated authorizations.")
                + formatParameter("--account-key",true)
                + formatParameter("--challenge-type, --dns-digests-dir, --domain or --csr, " +
                "--one-dir-for-well-known, --well-known-dir, --work-dir",false)
                + generateIndentString(INDENT_NUM)+"Needs work-dir file: order_uri_list\n");
        MAIN_USAGE.append("\n* "+COMMAND_GENERATE_CERTIFICATE+"\n"+wrapString("Generate a new certificate and "
                + "download it.")
                + formatParameter("--account-key, --csr",true)
                + formatParameter("--cert-dir, --work-dir",false)
                + generateIndentString(INDENT_NUM)+"Needs work-dir file: order_uri_list\n");
        MAIN_USAGE.append("\n* "+COMMAND_GET_AGREEMENT_URL+"\n"+wrapString("Returns the URL to the most recent "
                + "Subscriber Agreement. The URL is written to the JSON return value, before the status object, i.e "
                + "{\"agreement_url\":\"https://...\",\"status\":\"ok\"}."));
        MAIN_USAGE.append("\n* "+ COMMAND_ORDER_CERTIFICATE +"\n"+wrapString("Orders a certificate for the "
                + "specified domains, i.e. request generation of challenges and download them.")
                + formatParameter("--account-key, --csr",true)
                + formatParameter("--challenge-type, --dns-digests-dir, --one-dir-for-well-known, "
                + "--well-known-dir, --work-dir",false));
        MAIN_USAGE.append("\n* "+COMMAND_REGISTER+"\n"+wrapString("Create a new account with your CA which will "
                + "be associated with the specified user account key. This command doesn't create a new account if " +
                "the account is already exists with the specified account key.")
                + formatParameter("--account-key",true)
                + formatParameter("--email",false));
        MAIN_USAGE.append("\n* "+COMMAND_REVOKE_CERTIFICATE+"\n"+wrapString("Revoke certificates. You can revoke "
                + "either all your certificates or by time criteria. All certificates will be removed which are "
                + "generated after <from-time> and which will be expired by <to-time>.")
                + formatParameter("--account-key",true)
                + formatParameter("--from-time, --to-time, --work-dir",false)
                + generateIndentString(INDENT_NUM)+"Needs work-dir file: certificate_uri_list\n");
        MAIN_USAGE.append("\n* "+COMMAND_VERIFY_DOMAINS+"\n"+wrapString("Validate pending authorizations for "
                + "specified domains, i.e. verify challenges for pending authorizations. Only challenges for domains "
                + "that aren't authorized yet are verified (retrieved from your webserver by the CA). For domains "
                + "that already have a valid authorization (authorize-domains returned previously created and "
                + "already verified challenges), challenge files won't be checked.")
                + formatParameter("--account-key",true)
                + formatParameter("--challenge-type, --domain or --csr, --work-dir",false)
                + generateIndentString(INDENT_NUM)+"Needs work-dir file: order_uri_list\n");
        MAIN_USAGE.append("\n* " + COMMAND_HTTP_GET + "\n" + wrapString("A convenience command for executing "
                + "a HTTP GET to an arbitrary dynamic DNS update service. This provides "
                + "a mechanism to set the message digest on the DNS service for the ACME challenge. SAN certificates "
                + "are supported, in which case multiple calls will be made to update each DNS record. The "
                + "domain-tokens parameter is the password for your dynamic DNS update service. The "
                + "domain-tokens and domain-aliases parameters are specified as a comma separated list of "
                + "name=value pairs.")
                + formatParameter("--csr, --dns-digests-dir, --ddns-url, --domain-tokens", true)
                + formatParameter("--ddns-host-key, --ddns-record-key, --ddns-token-key,"
                + " --domain-aliases", false));
        MAIN_USAGE.append("\n* " + COMMAND_HTTP_POST + "\n" + wrapString("A convenience command for executing "
                + "a HTTP POST to an arbitrary dynamic DNS update service. This provides "
                + "a mechanism to set the message digest on the DNS service for the ACME challenge. SAN certificates "
                + "are supported, in which case multiple calls will be made to update each DNS record. The "
                + "domain-tokens parameter is the password for your dynamic DNS update service. The "
                + "domain-tokens and domain-aliases parameters are specified as a comma separated list of "
                + "name=value pairs.")
                + formatParameter("--csr, --dns-digests-dir, --ddns-url --domain-tokens", true)
                + formatParameter("--ddns-host-key, --ddns-record-key, --ddns-token-key,"
                + " --domain-aliases", false));
        
        MAIN_USAGE.append("\nExamples:\n");
        MAIN_USAGE.append(wrapString("\njava -jar acme_client.jar --command register -a /etc/pjac/account.key "
                + "--with-agreement-update --email admin@example.com"));
        MAIN_USAGE.append(wrapString("\njava -jar acme_client.jar --command order-certificate -a "
                + "/etc/pjac/account.key -w /etc/pjac/workdir/ --csr /etc/pjac/example.com.csr " +
                "--well-known-dir /var/www/.well-known/acme-challenge --one-dir-for-well-known"));
        MAIN_USAGE.append(wrapString("\njava -jar acme_client.jar --command verify-domains -a "
                + "/etc/pjac/account.key -w /etc/pjac/workdir/ --csr /etc/pjac/example.com.csr"));
        MAIN_USAGE.append(wrapString("\njava -jar acme_client.jar --command generate-certificate -a "
                + "/etc/pjac/account.key -w /etc/pjac/workdir/ --csr /etc/pjac/example.com.csr --cert-dir "
                + "/etc/pjac/certdir/"));
        MAIN_USAGE.append(wrapString("\njava -jar acme_client.jar --command http-get -csr "
                + "/etc/pjac/example.com.csr --dns-digests-dir /etc/pjac/digests/ --ddns-url "
                + "https://dyn.dns.example.com/nic/update --ddns-host-key hostname --ddns-record-key txt "
                + "--ddns-token-key password --domain-aliases "
                + "_acme-challenge.san-a.example.com=_my-cname.san-a.example.net, "
                + "_acme-challenge.san-b.example.com=_my-cname.san-b.example.net --domain-tokens "
                + "_my-cname.san-a.example.net=...OwOXZMK5IPtBN0..., "
                + "_my-cname.san-b.example.net=...N8BCp2imcCXbbd..."
                + ""));
        MAIN_USAGE.append("\n");
    }

    @Parameter(names = "--help", help = true, description = "Show help.")
    private boolean help;

    @Parameter(names = {"--version", "-v"}, help = true, description = "Show version information.")
    private boolean version;

    @Parameter(names = "--newest-only", description = "Download only the most recent certificate. " +
            "When omitted, all certificates are downloaded.")
    private boolean newestOnly;

    @Parameter(names = {"--work-dir", "-w"}, description = "Directory to save information about certificate orders " +
            "(order_uri_list) and about generated certificates (certificate_uri_list) to, for use with " +
            "later operations. These files contain no sensitive information. If order_uri_list is lost " +
            "you need to perform [certificate order](./Command-reference#order-certificate) again and if " +
            "certificate_uri_list is lost PJAC cannot download certificates or check expiration times of previously " +
            "generated certificates.")
    private String workDir = "/var/acme_work_dir/";


    @Parameter(names = {"--account-key", "-a"}, description = "User account key.")
    private String accountKey;

    @Parameter(names = {"--csr", "-c"}, description = "Certificate Signing Request (CSR).")
    private String csr;

    @Parameter(names = {"--domain", "-d"}, description = "Domain name. Can be used multiple times, up to CA's limit " +
            "(Let's Encrypt CA, for instance, has a limit of 100 domains for one certificate).")
    private Set<String> domains;

    @Parameter(names = {"--cert-dir"}, description = "The directory where downloaded certificates will be saved to.")
    private String certDir = this.workDir + "cert/";

    @Parameter(names = {"--log-dir"}, description = "The directory the log files will be saved to. This option can " +
            "be used with all commands.")
    private String logDir = "/var/log/acme/";

    @Parameter(names = {"--well-known-dir"}, description = "Directory to save challenge files to. All challenge files " +
            "must be accessible from internet via link: http://${domain}/.well-known/acme-challenge/${token}, where " +
            "${token} is the name of the challenge file and ${domain} is the domain name the challenge file corresponds to.")
    private String wellKnownDir = this.workDir + "well_known/";

    @Parameter(names = {"--dns-digests-dir"}, description = "Directory to save DNS digest files to.")
    private String dnsDigestDir = this.workDir + "dns_digests/";

    @Parameter(names = {"--email", "-e"}, description = "E-mail address to associate with an user account. Can be used to " +
            "i.a. retrieve an account if you lost your associated user account key (if supported by your provider) and to " +
            "receive notifications from the CA.")
    private String email;

    //acme://letsencrypt.org -- PRODUCTION
    //acme://letsencrypt.org/staging -- TEST
    //https://acme-staging-v02.api.letsencrypt.org/directory -- TEST
    //https://acme-v02.api.letsencrypt.org/directory -- PRODUCTION
    @Parameter(names = {"--server-url", "-u"}, description = "ACME Server URL. Can be specified to use a different CA " +
            "server, e.g. a staging server (test server). This option can be used with all commands.")
    private String acmeServerUrl = "https://acme-v02.api.letsencrypt.org/directory";

    @Parameter(names = {"--log-level"}, description = "Level of detail for logging. Possible values: " +
            "'OFF' - no logging; " +
            "'ERROR' - errors only; " +
            "'WARN' - errors and warnings; " +
            "'INFO' - errors, warnings and information; " +
            "'DEBUG' - errors, warnings, information and debug information; " +
            "'TRACE' - errors, warnings, information, debug information and operations tracing.")
    private String logLevel = "WARN";

    @Parameter(names = {"--one-dir-for-well-known"}, description = "By default challenge files will be saved " +
            "in separate directories on a per-domain basis. Use this option to save all downloaded challenge " +
            "files to one directory.")
    private boolean oneDirForWellKnown;

    @Parameter(names = {"--from-time"}, description = "Revoke all certificates which are generated after this " +
            "time. The time is specified in milliseconds since the UNIX epoch (January 1, 1970 00:00:00 UTC).")
    private long fromTime = Long.MIN_VALUE;

    @Parameter(names = {"--to-time"}, description = "Revoke all certificates which will expire before this " +
            "time. The time is specified in milliseconds since the UNIX epoch (January 1, 1970 00:00:00 UTC).")
    private long toTime = Long.MAX_VALUE;

    @Parameter(names = {"--challenge-type"}, description = "Challenge type to use when authorizing domains. " +
            "Possible values: HTTP01, DNS01.")
    private String challengeType = CHALLENGE_HTTP01;

    @Parameter(names = {"--with-agreement-update"}, description = "Automatically agree to the latest Subscriber " +
            "Agreement. Once in a while, the CA changes the Subscriber Agreement. Instead of manual updating " +
            "(agreeing) to the latest Subscriber Agreement (retrieve agreement URL, read the agreement and " +
            "update the agreement) each time the agreement is changed, you can automate this process. Don't " +
            "set this parameter if you don't want to agree with stuff you didn't read, but be aware that a " +
            "new Subscriber Agreement you didn't yet update (agree with) can break unattended operations. This " +
            "option can be used with all commands.")
    private boolean withAgreementUpdate;

    @Parameter(names = {"--command"}, description = "Command to execute. See below for an overview of " +
            "available commands.")
    private String command;

    @Parameter(names = {"--config-file", "-p"}, description = "Configuration properties file to load " +
            "default parameters from.")
    private String configFilename = "config.properties";

    @Parameter(names = {"--ddns-url"}, description = "The URL of your dynamic DNS service for use with " +
            "the http-get or http-post commands.")
    private String dynamicDnsUrl;

    @Parameter(names = {"--ddns-host-key"}, description = "The value of the param name of the host key " +
            "of your dynamic DNS service.")
    private String dynamicDnsHostKey = "hostname";

    @Parameter(names = {"--ddns-record-key"}, description = "The value of the param name of the record " +
            "key of your dynamic DNS service.")
    private String dynamicDnsRecordKey = "txt";

    @Parameter(names = {"--ddns-token-key"}, description = "The value of the param name of the auth " +
            "token key of your dynamic DNS service.")
    private String dynamicDnsTokenKey = "token";

    @Parameter(names = {"--ddns-pause-millis"}, description = "The time to pause between requests to the " +
            "dynamic DNS service when updating multiple records.")
    private int dynamicDnsPauseMillis = 1000;

    @Parameter(names = {"--domain-aliases"}, description = "A list of mappings of your domain names to " +
            "the alias records for update when using yuor dynamic DNS service.", 
            variableArity = true, listConverter = MapEntryConverter.class)
    private List<Map.Entry<String, String>> domainAliases;

    @Parameter(names = {"--domain-tokens"}, description = "A list of mappings of your domain names to the " +
            "tokens for authenticating with your dynamic DNS service.", 
            variableArity = true, listConverter = MapEntryConverter.class)
    private List<Map.Entry<String, String>> domainTokens;

    private boolean checkFile(String path, String errMsg) {
        if (path==null || !new File(path).isFile()) {
            LOG.error(errMsg);
            return false;
        }
        return true;
    }

    private boolean checkDir(String path, String errMsg) {
        if (path==null || !Files.isDirectory(Paths.get(path))) {
            LOG.error(errMsg);
            return false;
        }
        return true;
    }

    private boolean checkString(String str, String errMsg) {
        if (str == null || str.equals("")) {
            LOG.error(errMsg);
            return false;
        }
        return true;
    }

    private boolean checkAccountKey() {
        return checkFile(accountKey, "Your account key file doesn't exist: " + accountKey);
    }

    private boolean checkWorkDir() {
        return checkDir(workDir, "Your work directory doesn't exist: " + workDir);
    }

    private boolean checkCsr() {
        return checkFile(csr, "Your CSR file doesn't exist: " + csr);
    }

    private boolean checkDomains() {
        if (domains == null || domains.size() == 0) {
            LOG.error("You haven't provided any domain name");
            return false;
        }
        return true;
    }

    private boolean checkCertDir() {
        return checkDir(certDir, "Your certificates' directory doesn't exist: " + certDir);
    }


    private boolean checkWellKnownDir() {
        return checkDir(wellKnownDir, "Your well-known directory doesn't exist: " + wellKnownDir);
    }

    private boolean checkDnsDigestsDir() {
        return checkDir(dnsDigestDir, "Your dns-digests directory doesn't exist: " + dnsDigestDir);
    }

    private boolean checkEmail() {
        return checkString(email, "You have to provide email address");
    }

    private boolean checkOrderUriList() {
        return checkFile(Paths.get(workDir, ORDER_URI_LIST).toString(),
                "Your order uri list file doesn't exists. Seems that it is either wrong working directory or " +
                        "you haven't requested any certificates yet: " + Paths.get(workDir, ORDER_URI_LIST).toString());
    }

    private boolean checkCertificateUriList() {
        return checkFile(Paths.get(workDir, CERTIFICATE_URI_LIST).toString(),
                "Your certificate uri list file doesn't exists. Seems that it is either wrong working directory or " +
                        "you haven't got your certificates yet: " + Paths.get(workDir, CERTIFICATE_URI_LIST).toString());
    }

    private boolean checkChallengeType() {
        return challengeType.equalsIgnoreCase(CHALLENGE_HTTP01) || challengeType.equalsIgnoreCase(CHALLENGE_DNS01);
    }

    private boolean checkDynamicDnsUrl() {
        return checkString(dynamicDnsUrl, "You must provide the url of your dynamic DNS service");
    }

    public boolean verifyRequirements() {

        if (getCommand() == null) {
            LOG.error("No command specified. You must specify a command to execute, use --help for a list of available commands.");
            return false;
        }

        boolean correct;

        switch (command) {
            case Parameters.COMMAND_REGISTER:
                correct = checkAccountKey();
                break;
            case Parameters.COMMAND_GET_AGREEMENT_URL:
                correct = true;
                break;
            case Parameters.COMMAND_ADD_EMAIL:
                correct = checkAccountKey() && checkEmail();
                break;
            case Parameters.COMMAND_DEACTIVATE_ACCOUNT:
                correct = checkAccountKey();
                break;
            case Parameters.COMMAND_ORDER_CERTIFICATE:
                correct = checkAccountKey() && checkCsr() && checkChallengeType() && checkWorkDir();
                break;
            case Parameters.COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION:
                correct = checkAccountKey() && checkOrderUriList();
                break;
            case Parameters.COMMAND_DOWNLOAD_CHALLENGES:
                correct = checkAccountKey() && checkOrderUriList();
                break;
            case Parameters.COMMAND_VERIFY_DOMAINS:
                correct = checkAccountKey() && checkOrderUriList();
                break;
            case Parameters.COMMAND_GENERATE_CERTIFICATE:
                correct = checkAccountKey() && checkCsr() && checkWorkDir() && checkCertDir() && checkOrderUriList();
                break;
            case Parameters.COMMAND_DOWNLOAD_CERTIFICATES:
                correct = checkAccountKey() && checkCertificateUriList();
                break;
            case Parameters.COMMAND_REVOKE_CERTIFICATE:
                correct = checkAccountKey() && checkCertificateUriList();
                break;
            case Parameters.COMMAND_HTTP_GET:
                correct = checkCsr() && checkDnsDigestsDir() && checkDynamicDnsUrl();
                break;
            case Parameters.COMMAND_HTTP_POST:
                correct = checkCsr() && checkDnsDigestsDir() && checkDynamicDnsUrl();
                break;
            default:
                LOG.error("Command '" + command + "' not recognized. Use --help for a list of available commands");
                correct = false;
        }

        return correct;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean isVersion() {
        return version;
    }

    public String getWorkDir() {
        return workDir;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public String getCsr() {
        return csr;
    }

    public Set<String> getDomains() {
        return domains;
    }

    public String getCertDir() {
        return certDir;
    }

    public String getLogDir() {
        return logDir;
    }

    public String getWellKnownDir() {
        return wellKnownDir;
    }

    public String getDnsDigestDir() {
        return dnsDigestDir;
    }

    public String getEmail() {
        return email;
    }

    public String getAcmeServerUrl() {
        return acmeServerUrl;
    }

    public String getCommand() {
        return command;
    }

    public boolean isOneDirForWellKnown() {
        return oneDirForWellKnown;
    }

    public long getFromTime() {
        return fromTime;
    }

    public long getToTime() {
        return toTime;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public boolean isWithAgreementUpdate() {
        return withAgreementUpdate;
    }

    public boolean isNewestOnly() {
        return newestOnly;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public String getConfigFilename() {
        return configFilename;
    }

    public String getDynamicDnsUrl() {
        return dynamicDnsUrl;
    }

    public String getDynamicDnsHostKey() {
        return dynamicDnsHostKey;
    }

    public String getDynamicDnsRecordKey() {
        return dynamicDnsRecordKey;
    }

    public String getDynamicDnsTokenKey() {
        return dynamicDnsTokenKey;
    }
    
    public int getDynamicDnsPauseMillis() {
        return dynamicDnsPauseMillis;
    }

    public Map<String, String> getDomainAliases() {
        return toMap(domainAliases);
    }

    public Map<String, String> getDomainTokens() {
        return toMap(domainTokens);
    }
    
    protected static Map<String, String> toMap(List<Map.Entry<String, String>> list) {
        Map<String, String> map = new HashMap<String, String>();
        if (list != null) {
            for (Map.Entry<String, String> entry : list) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
}
