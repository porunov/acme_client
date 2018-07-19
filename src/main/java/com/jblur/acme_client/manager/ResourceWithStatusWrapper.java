package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;

/**
 * Created by Oleksandr Porunov on 7/20/18.
 */
public interface ResourceWithStatusWrapper {

    Status getStatus();

    void trigger() throws AcmeException;

    void update() throws AcmeException;

    String getLocation();

    void failIfInvalid() throws AcmeException;

}
