package com.jblur.acme_client.command.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;

/**
 * A convenience command for executing a HTTP POST to an arbitrary 
 * dynamic DNS update service. This provides a mechanism to set the message digest on
 * the DNS service for the ACME challenge.
 * 
 * @author Darian
 */
public class HttpPostCommand extends AbstractHttpCommand {

    private static final Logger LOG = LoggerFactory.getLogger(HttpPostCommand.class);

    public HttpPostCommand(final Parameters parameters)
            throws AccountKeyNotFoundException, AcmeException {
        
        super(parameters);
    }

    @Override
    protected Map<String, Object> sendRequest(final String url, final String requestParams) throws IOException {
        
        HttpURLConnection conn = openConnection(URI.create(url).toURL());
        conn.setRequestMethod("POST");
        conn.setRequestProperty(ACCEPT_HEADER, MIME_JSON);
        conn.setRequestProperty(ACCEPT_CHARSET_HEADER, DEFAULT_CHARSET);
        conn.setRequestProperty(ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toLanguageTag());
        conn.setRequestProperty(CONTENT_TYPE_HEADER, MIME_FORM);
        conn.setInstanceFollowRedirects(false);
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        
        byte[] outputData = requestParams.getBytes(StandardCharsets.UTF_8);
        
        conn.setRequestProperty(CONTENT_LENGTH_HEADER, Integer.toString(outputData.length));
        conn.setFixedLengthStreamingMode(outputData.length);
        
        conn.connect();
        
        try (OutputStream out = conn.getOutputStream()) {
            out.write(outputData);
        }
        
        int responseCode = conn.getResponseCode();
        String responseText = readResponse(conn.getInputStream());
        
        LOG.info("Response Code: " + responseCode);
        LOG.info("Response Text: " + responseText);
        
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Invalid response code: " + responseCode);
        }
        
        return parseResponse(responseCode, responseText);
    }
}
