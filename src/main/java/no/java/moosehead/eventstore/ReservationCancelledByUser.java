package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public class ReservationCancelledByUser extends AbstractEvent implements UserWorkshopEvent{
    private String email;
    private String workshopId;

    public ReservationCancelledByUser(long systemTimeInMillis, long revisionId, String email, String workshopId) {
        super(systemTimeInMillis, revisionId);
        this.email = email;
        this.workshopId = workshopId;
    }

    public ReservationCancelledByUser() {
    }

    public String getEmail() {
        return email;
    }

    public String getWorkshopId() {
        return workshopId;
    }
}
