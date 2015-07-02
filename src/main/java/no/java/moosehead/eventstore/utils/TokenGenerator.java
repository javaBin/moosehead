package no.java.moosehead.eventstore.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TokenGenerator {
    private AtomicLong revision = new AtomicLong(0L);

    public static String randomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public long nextRevisionId() {
        return revision.getAndIncrement();
    }

    public void resetRevision(long target) {
        revision.set(target);
    }
}
