package no.java.moosehead.eventstore.utils;

import java.util.concurrent.atomic.AtomicLong;

public class RevisionGenerator {
    private AtomicLong revision = new AtomicLong(0L);

    public long nextRevisionId() {
        return revision.getAndIncrement();
    }

    public void resetRevision(long target) {
        revision.set(target);
    }
}
