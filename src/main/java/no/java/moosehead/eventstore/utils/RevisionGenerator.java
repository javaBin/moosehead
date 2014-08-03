package no.java.moosehead.eventstore.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class RevisionGenerator {
    private AtomicLong revision = new AtomicLong(0L);
    private Random rnd = new Random();

    public long nextRevisionId() {
        long next = revision.getAndIncrement();
        int randPart = rnd.nextInt(900000) + 100000;
        StringBuilder res = new StringBuilder();
        res.append(randPart);
        res.append(next);
        return Long.parseLong(res.toString());
    }

    public void resetRevision(long target) {
        revision.set(target);
    }
}
