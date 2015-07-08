package no.java.moosehead.eventstore;

import java.time.Instant;

public class WorkshopAddedByAdmin extends WorkshopAddedEvent {

    public WorkshopAddedByAdmin() {
    }

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats);
    }

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats, Instant opens, Instant closes) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats, opens, closes);
    }

}
