package com.jblur.acme_client.command.http;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Application;
import com.jblur.acme_client.CSRParser;
import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.ACMECommand;
import com.jblur.acme_client.command.AccountKeyNotFoundException;

import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A convenience command for executing either a HTTP GET or HTTP POST to an arbitrary 
 * dynamic DNS update service. This provides a mechanism to set the message digest on
 * the DNS service for the ACME challenge.
 * 
 * @author Darian
 */
public abstract class AbstractHttpCommand extends ACMECommand {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpCommand.class);

    protected Proxy proxy = Proxy.NO_PROXY;
    protected int timeout = (int) Duration.ofSeconds(10).toMillis();
    
    protected static final String ACCEPT_HEADER = "Accept";
    protected static final String ACCEPT_CHARSET_HEADER = "Accept-Charset";
    protected static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    protected static final String CONTENT_LENGTH_HEADER = "Content-Length";
    protected static final String CONTENT_TYPE_HEADER = "Content-Type";
    protected static final String USER_AGENT_HEADER = "User-Agent";
    protected static final String DEFAULT_CHARSET = "utf-8";
    protected static final String MIME_JSON = "application/json";
    protected static final String MIME_FORM = "application/x-www-form-urlencoded";
    protected static final String ACME_CHALLENGE_PREFIX = "_acme-challenge.";
    protected static final String WILDCARD_PREFIX = "*.";
    protected static final String DNS_DIGEST_SUFFIX ="_dns_digest";
    protected static final String DNS_DIGEST_WILDCARD_SUFFIX = "_dns_digest_wildcard";
    
    protected static final String USER_AGENT;

    static {
        StringBuilder agent = new StringBuilder("PJAC");
        
        try {
            agent.append('/').append(Application.class.getPackage().getImplementationVersion());
        } 
        catch (Exception ex) {
            // Ignore, just don't use a version
            LoggerFactory.getLogger(AbstractHttpCommand.class).warn("Could not read library version", ex);
        }
        
        agent.append(" Java/").append(System.getProperty("java.version"));
        USER_AGENT = agent.toString();
    }
    
    public AbstractHttpCommand(final Parameters parameters)
            throws AccountKeyNotFoundException, AcmeException {
        
        super(parameters);
    }

    @Override
    public void commandExecution() {
        
        Set<String> domains;
        
        try {
            domains = CSRParser.getDomains(IOManager.readCSR(getParameters().getCsr()));
        } 
        catch (IOException ex) {
            LOG.error("Cannot read CSR " + getParameters().getCsr());
            error = true;
            return;
        }
        
        List<String> failedDomains = new LinkedList<>();
        
        for (String domain : domains) {
            try {
                String challenge = readChallenge(domain);
                execteRequest(domain, challenge);
            } 
            catch (IOException ex) {
                LOG.error("Cannot update domain record for " + domain, ex);
                failedDomains.add(domain);
            }
        }
        
        if (failedDomains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains, new TypeToken<List<String>>() {
            }.getType());
            result.add("failed_domains", failedDomainsJsonElement);
            error = true;
        }
    }
    
    protected String readChallenge(final String domain) throws IOException {
        
        String fileSuffix = domain.startsWith(WILDCARD_PREFIX) ? DNS_DIGEST_WILDCARD_SUFFIX : DNS_DIGEST_SUFFIX;
        
        String digest = IOManager.readString(
                Paths.get(getParameters().getDnsDigestDir(),
                        domain + fileSuffix).toString()
        );
        
        return digest;
    }
    
    protected void execteRequest(final String domain, final String challenge) throws IOException {
        
        String record = domain.startsWith(WILDCARD_PREFIX) ? 
                domain.substring(WILDCARD_PREFIX.length(), domain.length()) : domain;
        
        record = ACME_CHALLENGE_PREFIX + record;
        
        Parameters params = getParameters();
        
        String alias = params.getDomainAliases().get(record);
        String token = params.getDomainTokens().get(alias == null ? record : alias);
        
        String url = params.getDynamicDnsUrl();
        String hostKey = params.getDynamicDnsHostKey();
        String tokenKey = params.getDynamicDnsTokenKey();
        String recordKey = params.getDynamicDnsRecordKey();
        
        String requestParams = String.join("&", 
                String.join("=", hostKey, alias == null ? record : alias),
                String.join("=", tokenKey, token),
                String.join("=", recordKey, challenge));
        
        LOG.debug("URL {} [{}]", url, requestParams);
        
        Map<String, Object> responseMap = sendRequest(url, requestParams);
        
        JsonElement responseTextJsonElement = getGson().toJsonTree(responseMap, new TypeToken<Map<String, Object>>() {
        }.getType());
        
        result.add(domain, responseTextJsonElement);
    }
    
    protected abstract Map<String, Object> sendRequest(final String url, final String requestParams) throws IOException;
    
    protected HttpURLConnection openConnection(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
        configure(conn);
        return conn;
    }
    
    protected void configure(final HttpURLConnection conn) {
        
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setUseCaches(false);
        conn.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);
    }
    
    protected String readResponse(final InputStream stream) throws IOException {
        
        if (stream == null) {
            return null;
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        StringBuilder response = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
    
    protected Map<String, Object> parseResponse(final int responseCode, final String responseText) {
        
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("code", Integer.toString(responseCode));
        
        try {
            Object responseJson = getGson().fromJson(
                responseText, new TypeToken<Object>(){}.getType());
            
            responseMap.put("json", responseJson);
        } 
        catch (Exception ex) {
            // Probably not JSON, just put the text instead.
            responseMap.put("text", responseText);
        }
        
        return responseMap;
    }
}
