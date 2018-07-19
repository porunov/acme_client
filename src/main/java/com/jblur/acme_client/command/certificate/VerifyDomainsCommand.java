package com.jblur.acme_client.command.certificate;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import com.jblur.acme_client.manager.ChallengeManager;
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

public class VerifyDomainsCommand extends CertificateCommand {
    private static final Logger LOG = LoggerFactory.getLogger(VerifyDomainsCommand.class);

    public VerifyDomainsCommand(Parameters parameters) throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        List<Order> orderList = getNotExpiredOrders();
        if (orderList == null) {
            LOG.error("Cannot read file: " + ORDER_FILE_PATH);
            error = true;
            return;
        }

        String challengeType = getChallengeType();

        Set<String> domains = retrieveDomainsFromParametersOrCSR();

        if(error){
            LOG.error("Domains retrieval failed");
            return;
        }

        Set<String> verifiedDomains = new HashSet<>();

        Instant now = Instant.now();

        for(Order order : orderList){
            for(Authorization authorization : order.getAuthorizations()){
                String domain = getDomain(authorization);
                try {
                    if(domains!=null && (!domains.contains(domain)
                            || authorization.getExpires().isBefore(now))){
                        continue;
                    }
                }catch (Exception e){
                    LOG.warn("Cannot check authorization "+authorization.getLocation()
                            + " domain: "+domain);
                    continue;
                }

                if(authorization.getStatus().equals(Status.VALID)){
                    verifiedDomains.add(domain);
                    continue;
                }

                boolean validated = false;
                try {
                    ChallengeManager challengeManager = new ChallengeManager(authorization, challengeType);
                    validated = challengeManager.validateChallenge();
                } catch (AcmeException e) {
                    LOG.warn("Cannot validate one of challenges for domain: "+domain, e);
                }


                if(validated){
                    try {
                        validated = new AuthorizationManager(authorization).authorizeDomain();
                        if(validated) {
                            verifiedDomains.add(domain);
                        } else {
                            LOG.warn("Cannot authorize domain: "+domain+
                                    "\nAuthorization: "+authorization.getLocation().toString());
                        }
                    } catch (AcmeException e) {
                        LOG.warn("Cannot authorize domain: "+domain+
                                "\nAuthorization: "+authorization.getLocation().toString(), e);
                    }
                }

            }
        }

        if(domains != null) {
            domains.removeAll(verifiedDomains);

            for (String domain : domains) {
                LOG.error("Domain " + domain + " is not verified. Please, check warnings.");
            }

            error = error || !writeOrderList(orderList);

            if (domains.size() > 0) {
                JsonElement failedDomainsJsonElement = getGson().toJsonTree(domains, new TypeToken<HashSet<String>>() {
                }.getType());
                result.add("failed_domains", failedDomainsJsonElement);
                error = true;
            }
        }
    }
}
