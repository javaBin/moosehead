package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public class EmailConfimationSentByEmailSaga extends AbstractEvent {
    private long referingToEventWithRevision;

    public EmailConfimationSentByEmailSaga(long referingToEventWithRevision) {
        this.referingToEventWithRevision = referingToEventWithRevision;
    }

    public long getReferingToEventWithRevision() {
        return referingToEventWithRevision;
    }
}
