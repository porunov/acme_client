package com.jblur.acme_client.manager;

import com.jblur.acme_client.Application;
import org.shredzone.acme4j.Registration;
import org.shredzone.acme4j.RegistrationBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeConflictException;
import org.shredzone.acme4j.exception.AcmeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.security.KeyPair;

public class RegistrationManager {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private Session session;
    private Registration registration;

    public RegistrationManager(Registration registration) {
        this.registration = registration;
    }

    public RegistrationManager(Registration registration, Session session) throws AcmeException {
        this.session = session;
        this.registration = registration;
        this.registration.rebind(session);
    }

    public RegistrationManager(Session session) throws AcmeException {
        this.session = session;
        try {
            this.registration = new RegistrationBuilder().create(session);
        } catch (AcmeConflictException ex) {
            this.registration = Registration.bind(session, ex.getLocation());
            LOG.info("You already have an account. Account reestablished.");
        }
    }

    public RegistrationManager(Session session, String email) throws AcmeException {
        this.session = session;
        try {
            Registration registration = new RegistrationBuilder().addContact("mailto:" + email).create(this.session);
        } catch (AcmeConflictException ex) {
            this.registration = Registration.bind(session, ex.getLocation());
            LOG.info("You already have an account. Account reestablished.");
        }
    }

    public RegistrationManager(Session session, URI accountLocationUri) {
        this.session = session;
        this.registration = Registration.bind(session, accountLocationUri);
    }

    public Registration getRegistration() {
        return this.registration;
    }

    public void modifyAgreement(URI agreementUri) throws AcmeException {
        this.registration.modify()
                .setAgreement(agreementUri)
                .commit();
    }

    public void addContact(URI contactURI) throws AcmeException {
        this.registration.modify()
                .addContact(contactURI)
                .commit();
    }

    public void addContact(String email) throws AcmeException {
        this.registration.modify()
                .addContact("mailto:" + email)
                .commit();
    }

    public void changeAccountKeyPair(KeyPair keyPair) throws AcmeException {
        this.registration.changeKey(keyPair);
    }

    public void deactivateAccount() throws AcmeException {
        try {
            this.registration.deactivate();
        } catch (AcmeException e) {
            if (!e.getMessage().equals("HTTP 202: Accepted")) {
                throw e;
            }
        }
    }

}
