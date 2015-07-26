package no.java.moosehead.eventstore;

import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;

public class WorkshopAddedByAdmin extends WorkshopAddedEvent {
    private WorkshopData workshopData;

    public WorkshopAddedByAdmin() {
    }

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats);
    }

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats, Instant opens, Instant closes,WorkshopData workshopData) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats, opens, closes);
        this.workshopData = workshopData;
    }

    public WorkshopData getWorkshopData() {
        return workshopData;
    }
}
