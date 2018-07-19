package com.jblur.acme_client.command.certificate;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DownloadChallengesCommand extends CertificateCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadChallengesCommand.class);

    public DownloadChallengesCommand(Parameters parameters) throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        List<Order> orders = getNotExpiredOrders();

        Set<String> domains = retrieveDomainsFromParametersOrCSR();

        if(error){
            LOG.error("Domains retrieval failed");
            return;
        }

        Set<String> authorizedDomains = new HashSet<>();

        for(Order order : orders){
            for(Authorization authorization : order.getAuthorizations()){
                String domain = getDomain(authorization);
                if(domains == null || domains.contains(domain)){
                    try {
                        if(authorization.getExpires().isAfter(Instant.now()) &&
                                !authorization.getStatus().equals(Status.VALID)) {
                            writeChallengeByAuthorization(new AuthorizationManager(authorization));
                            authorizedDomains.add(domain);
                        }
                    } catch (Exception e) {
                        LOG.warn("Cannot get challenges for authorization "+authorization.getLocation().toString()+" " +
                                "(the authorization is for domain "+ domain +")", e);
                    }
                }
            }
        }

        error = error || !writeOrderList(orders);

        if(domains != null) {
            Set<String> failedDomains = new HashSet<>();

            for (String domain : domains) {
                if (!authorizedDomains.contains(domain)) {
                    LOG.error("Domain " + domain + " is not authorized. Please, authorize it first.");
                    failedDomains.add(domain);
                }
            }

            if (failedDomains.size() > 0) {
                JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains,
                        new TypeToken<HashSet<String>>() {
                        }.getType());
                result.add("failed_domains", failedDomainsJsonElement);
                error = true;
            }
        }

    }
}
