package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.BaseEvent;

public interface UserWorkshopEvent extends BaseEvent {
    public String getEmail();
    public String getWorkshopId();
}
