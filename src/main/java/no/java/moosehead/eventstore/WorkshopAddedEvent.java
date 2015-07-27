package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.repository.WorkshopData;

import java.time.Instant;
import java.util.Optional;

public abstract class WorkshopAddedEvent extends AbstractEvent {
    private String workshopId;
    private int numberOfSeats;
    private Instant startTime;
    private Instant endTime;

    public WorkshopAddedEvent(){
    }

    public WorkshopAddedEvent(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId);
        this.workshopId = workshopId;
        this.numberOfSeats = numberOfSeats;
    }

    public WorkshopAddedEvent(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats, Instant startTime, Instant endTime) {
        super(systemTimeInMillis, revisionId);
        this.workshopId = workshopId;
        this.numberOfSeats = numberOfSeats;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Optional<WorkshopData> getWorkshopData() {
        return Optional.empty();
    }
}
