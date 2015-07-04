package no.java.moosehead.eventstore;

public class WorkshopAddedBySystem extends WorkshopAddedEvent {

    public WorkshopAddedBySystem() {
    }

    public WorkshopAddedBySystem(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats);
    }

}
