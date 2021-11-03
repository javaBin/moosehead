package no.java.moosehead.eventstore;

import java.time.Instant;

public class WorkshopAddedBySystem extends WorkshopAddedEvent {

    public WorkshopAddedBySystem() {
    }

    public WorkshopAddedBySystem(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats);
    }

    public WorkshopAddedBySystem(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats, Instant startTime, Instant endTime) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats, startTime, endTime);
    }
}
