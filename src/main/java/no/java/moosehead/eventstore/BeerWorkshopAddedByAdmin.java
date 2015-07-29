package no.java.moosehead.eventstore;

import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;
import java.util.Optional;

public class BeerWorkshopAddedByAdmin extends WorkshopAddedEvent {
    private WorkshopData workshopData;

    public BeerWorkshopAddedByAdmin() {
    }

    public BeerWorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats, Instant opens, Instant closes, WorkshopData workshopData) {
        super(systemTimeInMillis, revisionId, workshopId, numberOfSeats, opens, closes);
        this.workshopData = workshopData;
    }

    public Optional<WorkshopData> getWorkshopData() {
        return Optional.of(workshopData);
    }
}
