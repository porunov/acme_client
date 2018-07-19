package com.jblur.acme_client.command.certificate;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jblur.acme_client.CSRParser;
import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.AuthorizationManager;
import com.jblur.acme_client.manager.OrderManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class OrderCertificateCommand extends CertificateCommand {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCertificateCommand.class);

    public OrderCertificateCommand(Parameters parameters)
            throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {
        List<String> failedDomains = new LinkedList<>();

        List<Order> savedOrders = getNotExpiredOrders();

        if(savedOrders==null){
            savedOrders = new LinkedList<>();
        }

        try {
            OrderManager orderManager = new OrderManager(getAccountManager().getAccount(),
                    CSRParser.getDomains(IOManager.readCSR(getParameters().getCsr())));

            savedOrders.add(orderManager.getOrder());

            for(Authorization authorization : orderManager.getOrder().getAuthorizations()){
                if(authorization.getStatus().equals(Status.VALID)){
                    continue;
                }
                String domain = getDomain(authorization);
                try {
                    LOG.info("Authorization created: "+authorization.getLocation());
                    writeChallengeByAuthorization(new AuthorizationManager(authorization));
                } catch (Exception ex) {
                    LOG.error("Cannot process authorization "+authorization.getLocation()
                            +" with domain "+domain, ex);
                    failedDomains.add(domain);
                }
            }

        } catch (AcmeException e) {
            LOG.error("Cannot authorize domains", e);
            error = true;
        } catch (IOException e) {
            LOG.error("Cannot read file: "+getParameters().getCsr(), e);
            error = true;
        }

        error = error || !writeOrderList(savedOrders);

        if (failedDomains.size() > 0) {
            JsonElement failedDomainsJsonElement = getGson().toJsonTree(failedDomains, new TypeToken<List<String>>() {
            }.getType());
            result.add("failed_domains", failedDomainsJsonElement);
            error=true;
        }
    }
}
