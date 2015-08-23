package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public class AbstractReservationCancelled extends AbstractEvent implements UserWorkshopEvent{
    private String email;
    private String workshopId;
    private int numSpotsCancelled;

    public AbstractReservationCancelled(long systemTimeInMillis, long revisionId, String email, String workshopId,int numSpotsCancelled) {
        super(systemTimeInMillis, revisionId);
        this.email = email;
        this.workshopId = workshopId;
        this.numSpotsCancelled = numSpotsCancelled;
    }

    public AbstractReservationCancelled() {
    }

    public String getEmail() {
        return email;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public int getNumSpotsCancelled() {
        return numSpotsCancelled;
    }
}
