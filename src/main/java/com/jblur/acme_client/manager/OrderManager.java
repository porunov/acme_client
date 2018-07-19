package com.jblur.acme_client.manager;

import org.shredzone.acme4j.*;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Set;

public class OrderManager {

    private static final Logger LOG = LoggerFactory.getLogger(OrderManager.class);

    private Order order;

    public OrderManager(Account account, Set<String> domainNames) throws AcmeException {
        this.order = account.newOrder().domains(domainNames).create();
    }

    public OrderManager(Account account, Set<String> domainNames, OrderInstants orderInstants) throws AcmeException {
        OrderBuilder orderBuilder = account.newOrder().domains(domainNames);
        if(orderInstants.getNotAfter().isPresent()){
            orderBuilder = orderBuilder.notAfter(orderInstants.getNotAfter().get());
        }
        if(orderInstants.getNotBefore().isPresent()){
            orderBuilder = orderBuilder.notBefore(orderInstants.getNotBefore().get());
        }
        this.order = orderBuilder.create();
    }

    public OrderManager(Login login, URL orderLocation){
        this.order = login.bindOrder(orderLocation);
    }

    public OrderManager(Order order){
        this.order = order;
    }

    public Order getOrder() {
        return this.order;
    }

    public void executeOrder(byte[] csr) throws AcmeException {
        this.order.execute(csr);
    }

    public boolean validateOrder() throws AcmeException{
        return ValidationService.validate(new ResourceWithStatusWrapper() {
            @Override
            public Status getStatus() {
                return order.getStatus();
            }

            @Override
            public void trigger() throws AcmeException {

            }

            @Override
            public void update() throws AcmeException {
                order.update();
            }

            @Override
            public String getLocation() {
                return order.getLocation().toString();
            }

            @Override
            public void failIfInvalid() throws AcmeException {
                if (order.getStatus() == Status.INVALID) {
                    throw new AcmeException("Order invalid: "+getLocation());
                }
                if (order.getStatus() == Status.PENDING) {
                    throw new AcmeException("Not all authorizations was completed for order to be validated: "+
                            getLocation());
                }
            }
        });
    }

}
