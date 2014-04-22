package no.java.moosehead.eventstore;

public class WorkshopAddedByAdmin extends AbstractEvent {

    public WorkshopAddedByAdmin(long systemTimeInMillis) {
        super(systemTimeInMillis);
    }
}
