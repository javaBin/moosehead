package no.java.moosehead.eventstore;

public class WorkshopAddedByAdmin extends WorkshopAddedEvent {

    public WorkshopAddedByAdmin() {
    }

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats);
    }

}
