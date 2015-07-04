package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public abstract class WorkshopAddedEvent extends AbstractEvent {
    private String workshopId;
    private int numberOfSeats;

    public WorkshopAddedEvent(){
    }

    public WorkshopAddedEvent(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId);
        this.workshopId = workshopId;
        this.numberOfSeats = numberOfSeats;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
}
