package com.jblur.acme_client;

import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Parameters {

    public final static String COMMAND_REGISTER = "register";
    public final static String COMMAND_GET_AGREEMENT_URL = "get-agreement-url";
    public final static String COMMAND_UPDATE_AGREEMENT = "update-agreement";
    public final static String COMMAND_ADD_EMAIL = "add-email";
    public final static String COMMAND_DEACTIVATE_ACCOUNT = "deactivate-account";
    public final static String COMMAND_AUTHORIZE_DOMAINS = "authorize-domains";
    public final static String COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION = "deactivate-domain-authorization";
    public final static String COMMAND_DOWNLOAD_CHALLENGES = "download-challenges";
    public final static String COMMAND_VERIFY_DOMAINS = "verify-domains";
    public final static String COMMAND_GENERATE_CERTIFICATE = "generate-certificate";
    public final static String COMMAND_DOWNLOAD_CERTIFICATES = "download-certificates";
    public final static String COMMAND_REVOKE_CERTIFICATE = "revoke-certificate";
    public final static String COMMAND_RENEW_CERTIFICATE = "renew-certificate";
    public final static String AUTHORIZATION_URI_LIST = "authorization_uri_list";
    public final static String CERTIFICATE_URI_LIST = "certificate_uri_list";
    public final static String CHALLENGE_HTTP01 = "HTTP01";
    public final static String CHALLENGE_DNS01 = "DNS01";
    public final static StringBuilder MAIN_USAGE = new StringBuilder();
    private static final Logger LOG = LoggerFactory.getLogger(Parameters.class);

    static {
        MAIN_USAGE.append("---Main commands---\n\n");
        MAIN_USAGE.append("Every time you run acme_client you must set the parameter command parameter '--command'\n");
        MAIN_USAGE.append("Optional parameters for all commands are: --log-dir, --log-level, " +
                "--server-url, --with-agreement-update, --agreement-url\n");
        MAIN_USAGE.append("For most of your commands you should specify working directory " +
                "for your account (--work-dir) but you can left it by default.\n");
        MAIN_USAGE.append("Every command returns a JSON object which always contains either \"status\":\"ok\" or " +
                "\"status\":\"error\" and sometimes an additional information. " +
                "You should check your log file if you get \"status\":\"error\".\n");
        MAIN_USAGE.append("\nBy default acme_client uses Letsencrypt's production server. I.e.:\n" +
                "'https://acme-v01.api.letsencrypt.org/directory'" +
                "\nIf you want to test the client then use a test server:\n" +
                "--server-url 'https://acme-staging.api.letsencrypt.org/directory'\n" +
                "If you use Letsencrypt's production server for testing you can reach limits " +
                "which are set by Letsencrypt (or your ACME provider).\n");
        MAIN_USAGE.append("\nCommands:\n");
        MAIN_USAGE.append("\n1) register - create a new account.\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --email\n");
        MAIN_USAGE.append("\n2) get-agreement-url - return a JSON object with 'agreement_ur' key where value " +
                "is the most up to date agreement. You must accept agreement if you want to use a service.\n" +
                "This command include `agreement_url` into JSON response " +
                "(I.e. {\"status\":\"ok\", \"agreement_url\":\"https://...\"}).\n" +
                "\tRequires parameters: --account-key\n");
        MAIN_USAGE.append("\n3) update-agreement - accept agreement. If you do not specify " +
                "'--agreement-url' you will automatically agree with the newest agreement.\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --agreement-url\n");
        MAIN_USAGE.append("\n4) add-email - add email to your account (some providers can recover your email" +
                " if you lose your account private key).\n" +
                "\tRequires parameters: --account-key, --email\n");
        MAIN_USAGE.append("\n5) deactivate-account - deactivate your account\n" +
                "\tRequires parameters: --account-key\n");
        MAIN_USAGE.append("\n6) authorize-domains  authorize specified domains. You must specify all " +
                "domains which you use in CSR (i.e. main domain and alternative domain names).\n" +
                "If you get \"status\":\"error\" this command may include domains which were not authorized " +
                "(\"failed_domains\":[\"example.com\", \"blog.example.com\"]). You can see the reason in your log file.\n" +
                "\tRequires parameters: --account-key, --domain\n" +
                "\tOptional parameters: --challenge-type\n");
        MAIN_USAGE.append("\n7) deactivate-domain-authorization - deactive domain authorization for specific " +
                "domain address (or for all if not specified) if you want to remove/sell your domain addresses.\n" +
                "If you get \"status\":\"error\" this command may include domains which were not deactivated " +
                "(\"failed_domains\":[\"example.com\", \"blog.example.com\"]). You can see the reason in your log file.\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --domain\n" +
                "\tMust have a file in working dir: authorization_uri_list\n");
        MAIN_USAGE.append("\n8) download-challenges - Download challenges from your authorizations.\n" +
                "If you get \"status\":\"error\" this command may include authorizations' locations from " +
                "which challenges wasn't been downloaded (\"failed_authorizations_to_download" +
                "\":[\"https://...\", \"https://...\"]). You can see the reason in your log file.\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --domain, --challenge-type\n" +
                "\tMust have file in working dir: authorization_uri_list\n");
        MAIN_USAGE.append("\n9) verify-domains - Check your challenges and verify your domains.\n" +
                "If you get \"status\":\"error\" this command may include domains which were not verified " +
                "(\"failed_domains\":[\"example.com\", \"blog.example.com\"]). You can see the reason in your log file.\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --domain\n" +
                "\tMust have a file in working dir: authorization_uri_list\n");
        MAIN_USAGE.append("\n10) generate-certificate - Generate new certificate.\n" +
                "\tRequires parameters: --account-key, --csr\n" +
                "\tOptional parameters: --cert-dir\n");
        MAIN_USAGE.append("\n11) download-certificate - Download your certificates which you have created earlier. " +
                "If you specify '--newest-only' then you will download only newest certificate. Without that parameter " +
                "you will download all certificates sorted by expiration date (i.e. cert_0.pem is the newest " +
                "and cert_15.pem is the oldest).\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --newest-only\n" +
                "\tMust have a file in working dir: certificate_uri_list\n");
        MAIN_USAGE.append("\n12) revoke-certificate - revoke certificates. You can revoke either all your " +
                "certificates or by time criteria. All certificates will be removed which are started after " +
                "'--from-time' and which will be expired till '--to-time'. " +
                "These parameters are written as GMT milliseconds.\n" +
                "If you get \"status\":\"error\" this command may include certificates' locations which were not " +
                "revoked (\"failed_certificates\":[\"https://...\", \"https://...\"]). " +
                "You can see the reason in your log file.\n" +
                "\tRequires parameters: --account-key\n" +
                "\tOptional parameters: --from-time, --to-time\n" +
                "\tMust have a file in working dir: certificate_uri_list\n");
        MAIN_USAGE.append("\n13) renew-certificate - Renew certificate either for existing CSR or for new CSR. " +
                "Will create a new certificate only if all your certificates will expire after '--max-expiration-time'. " +
                "'--max-expiration-time' is a time written in milliseconds " +
                "(By default it is 2592000000 which is equal to 30 days).\n" +
                "\tRequires parameters: --account-key, --csr\n" +
                "\tOptional parameters: --cert-dir, --max-expiration-time, --force.\n");
        MAIN_USAGE.append("\n");
    }

    @Parameter(names = "--help", help = true, description = "Show help.")
    private boolean help;

    @Parameter(names = "--newest-only", help = true, description = "If you want to download only the newest " +
            "certificate you can use this parameter. Otherwise you will download all your certificates.")
    private boolean newsetOnly;

    @Parameter(names = {"--work-dir", "-w"}, description = "Working directory to place authorization information " +
            "and certificates information (i.e. files 'authorization_uri_list' and 'certificate_uri_list'). " +
            "This files are public but you have to keep them safe to not lose them. Otherwise you have to " +
            "contact your provider to recover your lost certificates (Or create new ones).")
    private String workDir = "/var/acme_work_dir/";

    @Parameter(names = {"--account-key", "-a"}, description = "Your private key for account.")
    private String accountKey;

    @Parameter(names = {"--csr", "-c"}, description = "CSR file.")
    private String csr;

    @Parameter(names = {"--domain", "-d"}, description = "Domain addresses. Can be any count till providers " +
            "limits (I.e. Letsencrypt has 100 domains limit for one certificate).")
    private List<String> domains;

    @Parameter(names = {"--cert-dir"}, description = "Directory where generated certificates will be downloaded")
    private String certDir = this.workDir + "cert/";

    @Parameter(names = {"--log-dir"}, description = "Directory for log files")
    private String logDir = "/var/log/acme/";

    @Parameter(names = {"--well-known-dir"}, description = "Directory to place well-known files." +
            " All your well-known files must be accessible via link: http://${domain}/.well-known/acme-challenge/${token}" +
            " where ${token} is a name of a file and ${domain} is your domain name")
    private String wellKnownDir = this.workDir + "well_known/";

    @Parameter(names = {"--dns-digests-dir"}, description = "Directory to place dns digest files")
    private String dnsDigestDir = this.workDir + "dns_digests/";

    @Parameter(names = {"--email", "-e"}, description = "Email address to retrieve an account if you lost your " +
            "private key (If it is supported by your provider)")
    private String email;

    //acme://letsencrypt.org/staging  - TEST
    //https://acme-staging.api.letsencrypt.org/directory -- TEST
    //https://acme-v01.api.letsencrypt.org/directory -- PRODUCTION
    @Parameter(names = {"--server-url", "-u"}, description = "Acme api URL. You can use debug api for example")
    private String acmeServerUrl = "https://acme-v01.api.letsencrypt.org/directory";

    @Parameter(names = {"--agreement-url"}, description = "Url for agreement")
    private String agreementUrl;

    @Parameter(names = {"--max-expiration-time"}, description = "Set expiration time in milliseconds. " +
            "A certificate will be renewed only when your existed certificates will expire after max-expiration-time." +
            " By default it is 2592000000 milliseconds which is equal to 30 days. It means that when you call " +
            "'renew-certificate' command it will be renewed only if your certificate will expire in 30 days.")
    private long maxExpirationTime = 2592000000l;

    @Parameter(names = {"--log-level"}, description = "Log level. Can be one of the next: " +
            "'OFF' - without logs; " +
            "'ERROR' - only errors; " +
            "'WARN' - errors and warnings; " +
            "'INFO' - errors, warnings, information; " +
            "'DEBUG' - errors, warnings, information, debug; " +
            "'TRACE' - errors, warnings, information, debug, trace;")
    private String logLevel = "WARN";

    @Parameter(names = {"--force"}, description = "Force renewal if you don't want to check expiration " +
            "time before renew. Works the same as 'generate-certificate' command")
    private boolean force;

    @Parameter(names = {"--one-dir-for-well-known"}, description = "By default well-known files will be placed " +
            "separately for each domain (in different directories). Set this parameter if you want to " +
            "generate all challenges in the same directory.")
    private boolean oneDirForWellKnown;

    @Parameter(names = {"--from-time"}, description = "When you revoke your certificates you can set this option. " +
            "It means that all certificates which are started from this date will be revoked (date in milliseconds GMT.)")
    private long fromTime = Long.MIN_VALUE;

    @Parameter(names = {"--to-time"}, description = "When you revoke your certificates you can set this option. " +
            "It means that all certificates which will expire till this date will be revoked (date in milliseconds GMT.)")
    private long toTime = Long.MAX_VALUE;

    @Parameter(names = {"--challenge-type"}, description = "Challenge type (supported: HTTP01, DNS01. Default: HTTP01)")
    private String challengeType = CHALLENGE_HTTP01;

    @Parameter(names = {"--with-agreement-update"}, description = "Automatically updates agreement to the newest. " +
            "Some times provider can change an agreement and you should update the agreement link manually. " +
            "(I.e. get an agreement url, read an agreement, update an agreement). If you want to automate this process " +
            "you can use this parameter with each command you are using. You shouldn't use this parameter if you " +
            "are not agree with a new agreement or if you use different commands very often.")
    private boolean withAgreementUpdate;

    @Parameter(names = {"--command"}, description = "Command to execute. Command can be on of the next commands: " +
            "register, get-agreement-url, update-agreement, add-email, deactivate-account, authorize-domains, " +
            "deactivate-domain-authorization, download-challenges, verify-domains, generate-certificate," +
            "download-certificate, revoke-certificate, renew-certificate. " +
            "Read below 'Main commands' section to know how to use these commands.")
    private String command;

    private boolean checkFile(String path, String errMsg) {
        if (!new File(path).isFile()) {
            LOG.error(errMsg);
            return false;
        }
        return true;
    }

    private boolean checkDir(String path, String errMsg) {
        if (!Files.isDirectory(Paths.get(path))) {
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

    private boolean checkAgreementUrl() {
        return checkString(agreementUrl, "You have to provide agreement url address");
    }

    private boolean checkAuthorizationUriList() {
        return checkFile(Paths.get(workDir, AUTHORIZATION_URI_LIST).toString(),
                "Your authorization uri list file doesn't exists. Seems that it is either wrong working directory or " +
                        "you haven't authorized your domains yet: " + Paths.get(workDir, AUTHORIZATION_URI_LIST).toString());
    }

    private boolean checkCertificateUriList() {
        return checkFile(Paths.get(workDir, CERTIFICATE_URI_LIST).toString(),
                "Your certificate uri list file doesn't exists. Seems that it is either wrong working directory or " +
                        "you haven't got your certificates yet: " + Paths.get(workDir, CERTIFICATE_URI_LIST).toString());
    }

    private boolean checkChallengeType() {
        return challengeType.equalsIgnoreCase(CHALLENGE_HTTP01) || challengeType.equalsIgnoreCase(CHALLENGE_DNS01);
    }

    public boolean verifyRequirements() {

        if (getCommand() == null) {
            LOG.error("You must choose one of the commands you want to execute (--help)");
            return false;
        }

        boolean correct;

        switch (command) {
            case Parameters.COMMAND_REGISTER:
                correct = checkAccountKey();
                break;
            case Parameters.COMMAND_GET_AGREEMENT_URL:
                correct = checkAccountKey();
                break;
            case Parameters.COMMAND_UPDATE_AGREEMENT:
                correct = checkAccountKey();
                break;
            case Parameters.COMMAND_ADD_EMAIL:
                correct = checkAccountKey() && checkEmail();
                break;
            case Parameters.COMMAND_DEACTIVATE_ACCOUNT:
                correct = checkAccountKey();
                break;
            case Parameters.COMMAND_AUTHORIZE_DOMAINS:
                correct = checkAccountKey() && checkDomains() && checkChallengeType() && checkWorkDir();
                break;
            case Parameters.COMMAND_DEACTIVATE_DOMAIN_AUTHORIZATION:
                correct = checkAccountKey() && checkAuthorizationUriList();
                break;
            case Parameters.COMMAND_DOWNLOAD_CHALLENGES:
                correct = checkAccountKey() && checkAuthorizationUriList();
                break;
            case Parameters.COMMAND_VERIFY_DOMAINS:
                correct = checkAccountKey() && checkAuthorizationUriList();
                break;
            case Parameters.COMMAND_GENERATE_CERTIFICATE:
                correct = checkAccountKey() && checkCsr() && checkWorkDir() && checkCertDir();
                break;
            case Parameters.COMMAND_DOWNLOAD_CERTIFICATES:
                correct = checkAccountKey() && checkCertificateUriList();
                break;
            case Parameters.COMMAND_REVOKE_CERTIFICATE:
                correct = checkAccountKey() && checkCertificateUriList();
                break;
            case Parameters.COMMAND_RENEW_CERTIFICATE:
                correct = checkAccountKey() && checkCsr() && checkWorkDir() && checkCertDir();
                break;
            default:
                LOG.error("You must choose one of the commands you want to execute (--help)");
                correct = false;
        }

        return correct;
    }

    public boolean isHelp() {
        return help;
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

    public List<String> getDomains() {
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

    public String getAgreementUrl() {
        return agreementUrl;
    }

    public long getMaxExpirationTime() {
        return maxExpirationTime;
    }

    public String getCommand() {
        return command;
    }

    public boolean isForce() {
        return force;
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

    public boolean isNewsetOnly() {
        return newsetOnly;
    }

    public String getLogLevel() {
        return logLevel;
    }

}
