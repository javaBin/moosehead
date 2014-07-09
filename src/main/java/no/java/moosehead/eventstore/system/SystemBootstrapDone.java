package no.java.moosehead.eventstore.system;

import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.TransientEvent;


public class SystemBootstrapDone extends AbstractEvent implements TransientEvent{
    private Long eventStoreLastRevisionId;

    public SystemBootstrapDone(Long eventStoreLastRevisionId) {
        this.eventStoreLastRevisionId = eventStoreLastRevisionId;
    }

    public Long getEventStoreLastRevisionId() {
        return eventStoreLastRevisionId;
    }
}
