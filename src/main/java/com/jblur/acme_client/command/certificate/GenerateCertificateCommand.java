package com.jblur.acme_client.command.certificate;

import com.jblur.acme_client.CSRParser;
import com.jblur.acme_client.IOManager;
import com.jblur.acme_client.Parameters;
import com.jblur.acme_client.command.AccountKeyNotFoundException;
import com.jblur.acme_client.manager.CertificateManager;
import com.jblur.acme_client.manager.OrderManager;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class GenerateCertificateCommand extends CertificateCommand {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateCertificateCommand.class);

    public GenerateCertificateCommand(Parameters parameters)
            throws AccountKeyNotFoundException, AcmeException {
        super(parameters);
    }

    @Override
    public void commandExecution() {

        byte[] csrBytes;
        Set<String> domains;
        try {
            csrBytes = IOManager.readCSR(getParameters().getCsr());
            domains = CSRParser.getDomains(csrBytes);
        } catch (IOException e) {
            LOG.error("Cannot read CSR "+getParameters().getCsr());
            error=true;
            return;
        }

        List<Order> nonExpiredOrders = getNotExpiredOrders();
        if(nonExpiredOrders == null){
            LOG.error("Cannot read file: "+ORDER_FILE_PATH);
            error = true;
            return;
        }

        SortedSet<Order> orders = new TreeSet<>((o1, o2) -> {
            int result;

            if(o1.getExpires() != null && o2.getExpires() != null) {
                result = o2.getExpires().compareTo(o1.getExpires());
            } else {
                if(o1.getExpires() == null){
                    result = -1;
                } else if(o2.getExpires() == null){
                    result = 1;
                } else {
                    result = 0;
                }
            }

            return (result!=0)?result:Integer.compare(o1.hashCode(), o2.hashCode());
        });
        orders.addAll(nonExpiredOrders);

        for(Order order : orders){

            if(order.getStatus().equals(Status.VALID)){
                continue;
            }

            boolean validOrder = order.getAuthorizations().size()==domains.size();

            if(!validOrder){
                continue;
            }

            for(Authorization authorization : order.getAuthorizations()){
                if(authorization.getStatus() == Status.VALID && domains.contains(getDomain(authorization))){
                    continue;
                }
                validOrder = false;
            }

            if(!validOrder){
                continue;
            }

            try {
                order.execute(csrBytes);
            } catch (AcmeException e) {
                LOG.error("Cannot execute order "+order.getLocation().toString(), e);
                continue;
            }
            try {
                validOrder = new OrderManager(order).validateOrder();
            } catch (AcmeException e) {
                LOG.error("Cannot validate order "+order.getLocation().toString(), e);
                continue;
            }
            if(validOrder){
                generateCertificate(order);
                break;
            }

        }

    }

    private void generateCertificate(Order order){
        try {
            List<Certificate> certificateList = getNotExpiredCertificates();
            if(certificateList==null){
                certificateList = new LinkedList<>();
            }

            CertificateManager certificateManager = new CertificateManager(order.getCertificate());
            certificateList.add(certificateManager.getCertificate());
            writeCertificate(certificateManager, "");

            error = error || !writeCertificateList(certificateList);
        } catch (Exception e) {
            LOG.error("Cannot get certificate. Check if your domains of the certificate are verified", e);
            error = true;
        }

    }

}
