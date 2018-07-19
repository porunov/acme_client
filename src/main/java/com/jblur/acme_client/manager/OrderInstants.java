package com.jblur.acme_client.manager;

import java.time.Instant;
import java.util.Optional;

public class OrderInstants {

    private Instant notAfter;

    private Instant notBefore;

    public OrderInstants(Instant notAfter, Instant notBefore) {
        this.notAfter = notAfter;
        this.notBefore = notBefore;
    }

    public Optional<Instant> getNotAfter() {
        return Optional.ofNullable(notAfter);
    }

    public Optional<Instant> getNotBefore() {
        return Optional.ofNullable(notBefore);
    }
}
