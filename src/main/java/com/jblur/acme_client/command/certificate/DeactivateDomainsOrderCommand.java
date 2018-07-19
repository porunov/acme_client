package com.jblur.acme_client.command.certificate;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DeactivateDomainsOrderCommand extends CertificateCommand {
    private static final Logger LOG = LoggerFactory.getLogger(DeactivateDomainsOrderCommand.class);

    public DeactivateDomainsOrderCommand(Parameters parameters) throws AccountKeyNotFoundException,
            AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {

        List<Order> orders = getNotExpiredOrders();
        if (orders == null) {
            LOG.error("Cannot read file: " + ORDER_FILE_PATH);
            error = true;
            return;
        }

        List<String> failedAuthorizations = new LinkedList<>();

        List<Order> newOrderList = new LinkedList<>();

        Set<String> domains = retrieveDomainsFromParametersOrCSR();

        if(error){
            LOG.error("Domains retrieval failed");
            return;
        }

        for(Order order : orders) {
            boolean deactivated = false;
            for (Authorization authorization : order.getAuthorizations()) {
                String domain = getDomain(authorization);
                if (domains == null || domains.contains(domain)) {
                    try {
                        if(!authorization.getStatus().equals(Status.DEACTIVATED)) {
                            authorization.deactivate();
                        }
                        deactivated = true;
                    } catch (AcmeException e) {
                        LOG.error("Cannot deactivate authorization: " + authorization.getLocation().toString(), e);
                        failedAuthorizations.add(authorization.getLocation().toString());
                    }
                }
            }
            if(!deactivated){
                newOrderList.add(order);
            }
        }

        error = error || !writeOrderList(newOrderList);

        if (failedAuthorizations.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedAuthorizations, new TypeToken<List<String>>() {
            }.getType());
            result.add("failed_authorizations", failedDomainsJsonElement);
            error=true;
        }
    }

}
