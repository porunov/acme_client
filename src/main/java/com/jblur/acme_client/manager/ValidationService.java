package com.jblur.acme_client.manager;

import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeRetryAfterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Created by Oleksandr Porunov on 7/20/18.
 */
public class ValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationService.class);

    private static final long MAX_TIME_TO_WAIT = 1000 * 60 * 3;

    public static boolean validate(ResourceWithStatusWrapper resource) throws AcmeException {
        return validate(resource, MAX_TIME_TO_WAIT);
    }

    public static boolean validate(ResourceWithStatusWrapper resource, long maxTimeToWait) throws AcmeException {
        if(resource.getStatus() == Status.VALID){
            return true;
        }
        resource.trigger();
        long sleepTime = 3000L;
        long waitNoMore = System.currentTimeMillis()+maxTimeToWait;
        while (resource.getStatus() != Status.VALID) {
            try {
                resource.update();
                if(resource.getStatus() == Status.VALID){
                    break;
                }
            } catch (AcmeRetryAfterException e){
                Instant retryAfter = e.getRetryAfter();
                long sleepUntil = retryAfter.getEpochSecond();
                if(sleepUntil>waitNoMore){
                    throw new AcmeException("Resource isn't available to be updated right now. "+
                            "It will be possible to check for status after: "+retryAfter.toString());
                }
                try {
                    Thread.sleep(Math.max(0, sleepUntil-System.currentTimeMillis()));
                    continue;
                } catch (InterruptedException e2) {
                    LOG.warn("Sleep interrupted", e2);
                }
            }
            if(System.currentTimeMillis()>waitNoMore){
                throw new AcmeException("Waiting too long for resource to complete: "+resource.getLocation());
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                LOG.warn("Sleep was interrupted", e);
            }
            resource.failIfInvalid();
        }
        return resource.getStatus() == Status.VALID;
    }

}
